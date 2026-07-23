# Plan de Monetización Freemium - IrisWallpaper

## Modelo de Negocio
- **Free**: Packs limitados (8 de 31), 3 packs custom, solo CROP, solo Home screen, solo Unsplash
- **Premium (Iris Pro)**: 31 packs, 10 packs custom, todos los scale modes, Both/Lock screens, 3 fuentes de imágenes, reglas por temperatura
- **Precios**: $1.99/mes o $14.99/año
- **Sin login** — Google Play Billing maneja identidad
- **Sin server** para MVP — validación local con SharedPreferences
- **Testing**: License Testing en Google Play Console (el dueño no paga)

---

## FASE 0: Dependencias y Fundación

### 0.1 Dependencias
- `gradle/libs.versions.toml`: agregar billing 7.1.1
- `app/build.gradle.kts`: `implementation(libs.androidx.billing.ktx)`
- `AndroidManifest.xml`: permiso `com.android.vending.BILLING`

### 0.2 PremiumRepository
- Interface en `domain/repository/PremiumRepository.kt`
- Impl en `data/premium/PremiumRepositoryImpl.kt` (SharedPreferences)
- En debug: siempre premium para testing

### 0.3 Koin
- Inyectar PremiumRepository en DataModule.kt

### 0.4 Strings
- Agregar strings premium en EN y ES

---

## FASE 1: Gating de Packs Predefinidos

### PredefinedPack.kt
- Agregar `isPremium: Boolean = false`
- 8 packs gratis, 23 premium en weatherPacks
- 2 gratis, 8 premium en weeklyPacks
- 1 gratis, 4 premium en timePacks
- 1 gratis, 3 premium en randomPacks

### SuggestedPack.kt + SearchScreen.kt
- Cards premium con overlay gris + candado + badge "PRO"
- onClick → upsell en vez de abrir pack

### PackDetail.kt
- Botón "Install" → "Unlock with Iris Pro" para packs premium

---

## FASE 2: Gating de Packs Custom

### UserPreferencesRepositoryImpl.kt
- `addNewPack()`: límite dinámico (3 free, 10 premium)

### PackSelectorSection.kt
- Texto "(X/3)" free o "(X/10)" premium
- Botón "+" → upsell al límite

---

## FASE 3: Gating ScaleMode + Target + Sources

### ScaleModeSelector.kt
- STRETCH y FIT: chip deshabilitado + candado + upsell

### WallpaperDetailSheet.kt + WallpaperSearchResultItem.kt
- Target Both (3) y Lock (2): candado + upsell
- Home (1): siempre gratis

### UnifiedImageRepositoryImpl.kt
- Pexels y Pixabay: solo premium

---

## FASE 4: Reglas por Temperatura (Premium)

### CurrentWeather.kt
- Agregar `temperature: Double = 0.0`

### WeatherRepository.kt + WeatherRepositoryImpl.kt
- Agregar temperatura a WeatherInfo

### TemperatureRule.kt (nuevo)
- TemperatureRange: FREEZING, COLD, COOL, WARM, HOT
- Map key: "FREEZING-DAWN", "HOT-NIGHT", etc.

### WallpaperConfig.kt
- Agregar `temperatureRules: Map<String, String>`

### ResolveWallpaperUseCaseImpl.kt
- Paso entre daily rules y weather rules para temperature rules

### TemperatureRuleSection.kt (nuevo)
- 5 rangos × 4 horarios = 20 slots
- Solo visible si premium

---

## FASE 5: Google Play Billing

### BillingManager.kt (nuevo)
- BillingClient connection
- SKUs: iris_pro_monthly, iris_pro_yearly
- launchBillingFlow, queryPurchases, onPurchasesUpdated

### PremiumRepositoryImpl.kt
- Integrar con BillingManager para validación

### WallpaperConfigScreen.kt
- Sección "Iris Pro" con estado y botón upgrade

---

## FASE 6: PremiumUpsellSheet

### PremiumUpsellSheet.kt (nuevo)
- Bottom sheet reutilizable
- Lista de features premium
- Botones Monthly + Yearly
- Restore Purchases

---

## Archivos Totales: ~35
- 7 nuevos
- 28 modificados

## Tiempo Estimado: ~10 días
