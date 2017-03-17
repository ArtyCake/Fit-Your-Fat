# Fit Your Fat
[Play Market](https://play.google.com/store/apps/details?id=com.artycake.fityourfat) | [Demo video](https://youtu.be/A8gNM5Ku5Ew)

A simple and convenient timer for your workouts.

### Main features
- A timer for interval training, running in the background
- Notifications about changing exercises with sounds, voice and vibration
- Pause the timer on an incoming call
- Unlimited training and exercises
- No advertising and in-app purchases, do train and do not be distracted by anything

---

Aplication was created for demonstration purposes. 

### Libraries used in this project
- [Realm](https://realm.io/docs/java/latest/) for storing forecasts
- [Butterknife](http://jakewharton.github.io/butterknife/) for views binding
- [CircleProgress](https://github.com/lzyzsd/CircleProgress) for main timer interface

### Features realised in this project
- Background service that makes all calculation and send broadcasts to update UI
- Fragments with fragmentManager and viewPager
- Notification with buttons
- Playing sounds, vibrations and textToSpeach
- BroadcastReceiver for incomming calls to pause and resume timer
