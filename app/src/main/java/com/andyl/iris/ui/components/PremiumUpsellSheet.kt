package com.andyl.iris.ui.components

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.andyl.iris.R
import com.andyl.iris.billing.BillingManager
import com.android.billingclient.api.ProductDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumUpsellSheet(
    billingManager: BillingManager,
    onDismiss: () -> Unit
) {
    val productDetails by billingManager.productDetails.collectAsState()
    val purchaseResult by billingManager.purchaseResult.collectAsState()
    val activity = LocalContext.current as? Activity
    var showPostPurchase by remember { mutableStateOf(false) }

    when (purchaseResult) {
        is BillingManager.PurchaseResult.Success -> {
            showPostPurchase = true
            billingManager.resetPurchaseResult()
        }
        is BillingManager.PurchaseResult.Cancelled -> {
            billingManager.resetPurchaseResult()
        }
        is BillingManager.PurchaseResult.Error -> {
            billingManager.resetPurchaseResult()
        }
        is BillingManager.PurchaseResult.None -> {}
    }

    if (showPostPurchase) {
        PostPurchaseDialog(
            onDismiss = {
                showPostPurchase = false
                onDismiss()
            }
        )
    }

    val monthlyDetails = productDetails.find { it.productId == BillingManager.PRODUCT_ID_MONTHLY }
    val yearlyDetails = productDetails.find { it.productId == BillingManager.PRODUCT_ID_YEARLY }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .padding(bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                ) {}
            }

            Text(
                text = "PRO",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.upsell_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.upsell_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Feature list
            FeatureItem(text = stringResource(R.string.upsell_feature_packs))
            FeatureItem(text = stringResource(R.string.upsell_feature_custom))
            FeatureItem(text = stringResource(R.string.upsell_feature_modes))
            FeatureItem(text = stringResource(R.string.upsell_feature_screens))
            FeatureItem(text = stringResource(R.string.upsell_feature_sources))
            FeatureItem(text = stringResource(R.string.upsell_feature_temp))

            Spacer(modifier = Modifier.height(24.dp))

            // Monthly plan
            monthlyDetails?.let { details ->
                val pricing = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases
                    ?.pricingPhaseList?.firstOrNull()
                val priceText = pricing?.formattedPrice ?: "$1.99/mes"

                PlanCard(
                    title = stringResource(R.string.premium_monthly),
                    price = priceText,
                    isSelected = false,
                    onClick = {
                        activity?.let { billingManager.launchBillingFlow(it, details) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Yearly plan
            yearlyDetails?.let { details ->
                val pricing = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases
                    ?.pricingPhaseList?.firstOrNull()
                val priceText = pricing?.formattedPrice ?: "$14.99/año"

                PlanCard(
                    title = stringResource(R.string.premium_yearly),
                    price = priceText,
                    isSelected = false,
                    isRecommended = true,
                    onClick = {
                        activity?.let { billingManager.launchBillingFlow(it, details) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Fallback if products not loaded yet
            if (monthlyDetails == null && yearlyDetails == null) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.loading_plans),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Restore purchases
            TextButton(
                onClick = { billingManager.queryExistingPurchases() }
            ) {
                Text(
                    text = stringResource(R.string.restore_purchases),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.btn_close))
            }
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    isSelected: Boolean,
    isRecommended: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = if (isRecommended) 2.dp else 1.dp,
            color = if (isRecommended) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isRecommended) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "BEST VALUE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                Text(
                    text = price,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
