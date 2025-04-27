package com.ghoast.billing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BillingViewModel : ViewModel() {

    // Για αποθήκευση κατάστασης πληρωμής αν το χρειαστούμε
    private val _paymentSuccess = MutableStateFlow(false)
    val paymentSuccess: StateFlow<Boolean> = _paymentSuccess

    /**
     * Ξεκινάει το flow πληρωμής για Pay-per-Offer (0.99€)
     */
    suspend fun launchPayPerOfferFlow(): Boolean {
        // 🔥 Προσωρινή προσομοίωση επιτυχίας
        _paymentSuccess.value = true
        return true
    }

    /**
     * Ξεκινάει το flow πληρωμής για Subscription (4.99€/μήνα ή 49.99€/έτος)
     */
    suspend fun launchSubscriptionFlow(): Boolean {
        // 🔥 Προσωρινή προσομοίωση επιτυχίας
        _paymentSuccess.value = true
        return true
    }
}
