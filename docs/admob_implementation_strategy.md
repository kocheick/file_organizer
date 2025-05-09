# AdMob Implementation Strategy for File Organizer App

This document outlines our strategy for implementing AdMob ads in the File Organizer app in a way that balances revenue
generation with user experience.

## Ad Types & Placement

### Banner Ads

1. **Main Screen Bottom Banner**
    - Place a banner ad at the bottom of the MainScreen using Scaffold's `bottomBar` parameter
    - Implementation location: `MainScreen.kt` Scaffold component
    - Priority: High (Phase 1) - easiest implementation with minimal user disruption

2. **Between Task Items**
    - Insert native banner ads in the task list (every 5-7 items)
    - Implementation: Modify `TaskListContent` composable to inject ad items at regular intervals
   ```kotlin
   LazyColumn(...) {
       itemsIndexed(tasksList) { index, task ->
           TaskItem(...)
           
           // Insert ad after every 5 items
           if (index > 0 && index % 5 == 0) {
               AdmobBanner()
           }
       }
   }
   ```
    - Priority: Medium (Phase 2) - implements after basic banner ads

3. **Rule Management Screen**
    - Add a banner at the bottom of the rule management screen
    - Implementation: Modify `RuleManagementScreen.kt` to include banner in the Scaffold
    - Priority: Medium (Phase 2)

### Interstitial Ads

1. **Post-Operation Interstitials**
    - Show after completing file organization operations
    - Implementation: In `MainViewModel`, display ad after `sortFiles()` completes
    - Frequency cap: Maximum once per 10-minute period
    - Priority: Medium (Phase 2)

2. **Screen Transition Interstitials**
    - Show occasionally when navigating between main app sections
    - Implementation: In `Navigation.kt`, add logic with frequency cap (once per session)
    - Exclude transitions to/from task creation and editing screens
    - Priority: Low (Phase 3) - implement only if revenue needs boosting

### Rewarded Ads

1. **Temporary Premium Features**
    - Allow users to unlock premium features for a limited time by watching ads
    - Example premium features:
        - Advanced rule conditions (regex matching, file size filters)
        - Batch operations for multiple file types
        - Priority processing for large operations
        - Ad-free experience for a few hours
    - Implementation: Add "Get Premium Feature" button in appropriate screens
    - Priority: Medium-High (Phase 2) - provides higher revenue and better user experience

2. **Operation Boosters**
    - Allow users to "boost" operations by watching an ad
        - Example: Process files in background with higher priority
    - Implementation: Add "Boost Operation" option before starting large file operations
    - Priority: Medium (Phase 2)

### Native Ads

1. **Stats Section Integration**
    - Integrate native ads that blend with the Stats component design
    - Implementation: Modify `Stats` composable to occasionally include native ads
    - Priority: Low (Phase 3)

2. **Empty State Ad**
    - Show native ad in empty state screen when no tasks are configured
    - Implementation: Enhance `EmptyContentComponent`
    - Priority: Medium (Phase 2)

## Implementation Approach

### Architecture

1. **Ad Manager Service**
    - Create a dedicated `AdService` to handle ad loading, caching and display
    - Inject via Koin to maintain clean architecture
    - Separate ad logic from business logic

2. **Ad View Components**
    - Create reusable Composable ad components:
        - `AdBanner.kt` - For banner ads
        - `AdInterstitial.kt` - For interstitial ad handling
        - `AdRewarded.kt` - For rewarded ad implementation

3. **Configuration**
    - Store ad unit IDs in `build.gradle` variants for easy test/production switching
    - Create test flavor with test ad units

### Phased Rollout

1. **Phase 1 (Week 1-2)**
    - Implement bottom banner on main screen only
    - Set up basic ad infrastructure
    - Gather baseline metrics

2. **Phase 2 (Week 3-4)**
    - Add rewarded ads for premium features
    - Implement post-operation interstitials
    - Add in-list banner ads

3. **Phase 3 (Week 5-6)**
    - Add native ads in stats section
    - Implement screen transition interstitials
    - Fine-tune ad frequency based on metrics

### User Experience Considerations

1. **Frequency Capping**
    - Interstitials: Maximum 2 per session, at least 3 minutes apart
    - Rewarded: No limits (user-initiated)
    - Banners: Refresh every 60 seconds

2. **Performance**
    - Preload ads to prevent UI lag
    - Handle ad loading errors gracefully
    - Don't block UI rendering for ad loading

3. **Ad-Free Option**
    - Implement a one-time purchase option to remove all ads
    - Price point: $3.99-$5.99 based on market research

## Revenue Optimization

1. **A/B Testing**
    - Test different ad placements and frequencies
    - Measure impact on:
        - User retention
        - Average session length
        - Revenue per DAU

2. **Ad Network Mediation**
    - Use AdMob mediation to optimize fill rates
    - Include AdMob, Meta Audience Network, and Unity Ads

3. **Rewarded Ad Incentives**
    - Test different reward values to maximize completion rates
    - Track conversion from rewarded ad viewers to paying customers

## Metrics & KPIs

1. **Performance Metrics**
    - Ad impression per session
    - Ad click-through rate (CTR)
    - Revenue per daily active user (ARPDAU)

2. **User Experience Metrics**
    - App rating before/after ad implementation
    - Session length and frequency
    - User retention (7-day, 30-day)

## Conclusion

This strategy aims to monetize the app while preserving the core user experience. By implementing ads at natural break
points and providing rewarded options that deliver value, we can generate revenue without significantly impacting user
satisfaction.

The phased approach allows us to measure the impact of each ad type and optimize accordingly, ensuring we maximize
revenue while maintaining strong user engagement metrics.