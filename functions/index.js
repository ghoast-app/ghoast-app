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
    const favoriteShopsSnapshot = await db.collection("users").doc(userId).collection("favorite_shops").get();

    const hasFavorited = favoriteShopsSnapshot.docs.some(doc => doc.data().shopId === shopId);
    if (hasFavorited) {
      const fcmToken = userDoc.data().fcmToken;
      if (fcmToken) {
        tokens.push(fcmToken);
      }
    }
  }

  if (tokens.length > 0) {
    await getMessaging().sendEachForMulticast({
      tokens,
      notification: {
        title: `🛍️ Νέα προσφορά από το ${offer.shopName}`,
        body: offer.title,
      },
    });
    console.log(`✅ Στάλθηκε notification σε ${tokens.length} χρήστες.`);
  } else {
    console.log("⚠️ Κανένας χρήστης δεν είχε αποθηκευμένο αυτό το κατάστημα στα αγαπημένα.");
  }
});
