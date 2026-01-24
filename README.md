# Companion Shimejis (Digital Pets) for Android

**Companion Shimejis** is an Android application that brings delightful, interactive digital pets to your screen. Unlike static wallpapers, these pets live in an overlay layer, allowing them to follow you across different apps, interact with your screen, and provide a sense of companionship.

---

## 🌟 Current Features (MVP)

The project currently supports the following core "Minimum Viable Product" features:
- **System Overlay Pets**: Characters are drawn over other applications using the `SYSTEM_ALERT_WINDOW` permission.
- **Multi-Pet Support**: Spawn and manage multiple pets simultaneously.
- **Drag & Drop**: Manually reposition pets anywhere on your screen.
- **Basic Physics**: Simple movement and boundary detection.
- **Foreground Service Lifecycle**: A robust foreground service ensures pets stay active even when the app is closed, with automatic pausing when the screen is off to save battery.

---

## 🛠️ Getting Started

### Prerequisites
- **Android Device**: Supports Android 8.0 (API 26) and above.
- **Overlay Permission**: The app requires the "Draw over other apps" permission to function.

### Installation
1. Clone the repository.
2. Build and run the app using Android Studio.
3. Upon first launch, the app will request the necessary overlay permission.
4. Select your favorite pets from the dashboard and toggle "Start Pets Floating".

---

## 🧠 Design Philosophy

Our development is guided by a commitment to **Believable Companionship**:
- **On-Device Logic**: All behavior is calculated locally; no cloud dependencies.
- **Privacy First**: No accessibility abuse, no screen reading, and no input monitoring.
- **Deterministic Behavior**: Actions are explainable and testable, not chaotic.
- **Polite UX**: The pets are guests on your screen; they never disrupt your work or hide critical UI elements.

---

## 🚀 Future Enhancements & Roadmap

We are committed to bringing these pets to life through incremental, ethical, and technically realistic updates.

### Phase 1: Behavior & Animation Foundation
- **Behavior State Machine**: Moving from chaotic physics to intentional states (IDLE, WALK, FALL, SLEEP, INTERACT).
- **Sprite Sheet Animation**: Support for frame-based animations for fluid movements (Sit, Sleep, Climb).
- **Interaction Polish**: Dedicated animations for dragging and tapping.

### Phase 2: Desktop-Grade "Shimeji" Feel
- **Gravity & Surface Affinity**: Pets will fall to the "floor" (screen bottom) and walk along edges instead of floating randomly.
- **Climbing & Hanging**: Pets will be able to climb the sides of your screen or hang from the top notification bar.

### Phase 3: Emotional Continuity
- **Mood Model**: Lightweight internal stats like Energy, Mood (Happy/Bored), and Affinity (bond with the user).
- **Personality Profiles**: Distinct behavior multipliers (e.g., a "Lazy" pet vs. a "Playful" pet).
- **Safe Local Memory**: Pets remember their favorite screen corners or interaction history without tracking sensitive data.

### Phase 4: Context Awareness
- **System Signals**: Reacting to time of day, battery levels, charging state, and DND (Do Not Disturb) mode.
- **Contextual Behavior**: Pets might sleep when the battery is low or hide politely when DND is enabled.

### Phase 5: Aesthetic & User Experience
- **Politeness UX**: Auto-hide during full-screen video and temporary hiding while typing.
- **Visual Delight**: "Emote" bubbles (!, ?, ❤️, zZz) to communicate pet thoughts.
- **Seasonal Themes**: Visual skins for holidays and special events.

### Phase 6: Advanced Expansion
- **Multi-Pet Interaction**: Pets acknowledging and reacting to each other.
- **Community Packs**: Importing standard desktop Shimeji packs (XML/JSON) from external sources.

---

## 🔋 Performance & Battery Strategy
We prioritize your device's health:
- Shared frame tickers to minimize CPU usage.
- Frame rate throttling when pets are idle.
- Zero "busy loops" and minimal memory allocations.

---

## 🚫 Non-Goals
To maintain user trust, we will **never**:
- Use Accessibility Services.
- Read your screen or track app usage.
- Send push notification spam.
- Require "Always-on" listening or microphones.
