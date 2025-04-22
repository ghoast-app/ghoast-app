import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";

initializeApp();

export const sendNotificationOnNewOffer = onDocumentCreated("offers/{offerId}", async (event) => {
  const offer = event.data?.data();
  if (!offer) return;

  const shopId = offer.shopId;
  const db = getFirestore();

  const usersSnapshot = await db.collection("users").get();
  const tokens = [];

  for (const userDoc of usersSnapshot.docs) {
    const userId = userDoc.id;

    // ✅ Ελέγχουμε αν έχει στα αγαπημένα το κατάστημα
    const favoriteShopsSnapshot = await db
      .collection("users")
      .doc(userId)
      .collection("favorite_shops")
      .get();

    const hasFavorited = favoriteShopsSnapshot.docs.some(
      doc => doc.data().shopId === shopId
    );

    if (hasFavorited) {
      const userData = userDoc.data();
      const fcmToken = userData.fcmToken;

      if (fcmToken) {
        tokens.push({ token: fcmToken, userId });
      }
    }
  }

  if (tokens.length > 0) {
    // ✅ Στέλνουμε ειδοποίηση με data-only payload
    const responses = await Promise.all(tokens.map(({ token, userId }) => {
      const payload = {
        token,
        data: {
          title: `Νέα προσφορά από το ${offer.shopName}`,
          body: offer.title,
          shopId: shopId,
          offerId: offer.id || "", // αν έχει id
        },
      };

      // ✅ Αποθήκευση στο Firestore για να εμφανίζεται στο app
      const notificationRef = db
        .collection("users")
        .doc(userId)
        .collection("notifications")
        .doc();

      const notificationData = {
        id: notificationRef.id,
        title: payload.data.title,
        body: payload.data.body,
        offerId: payload.data.offerId,
        shopId: payload.data.shopId,
        timestamp: Date.now(),
      };

      return Promise.all([
        getMessaging().send(payload),
        notificationRef.set(notificationData),
      ]);
    }));

    console.log(`✅ Στάλθηκαν ${responses.length} ειδοποιήσεις.`);
  } else {
    console.log("⚠️ Κανένας χρήστης δεν είχε αποθηκευμένο αυτό το κατάστημα στα αγαπημένα.");
  }
});
