# BattleZone - Esports Tournament App

A full-stack esports tournament platform for mobile gaming (Free Fire MAX, PUBG Mobile, COD Mobile).

## Stack
- **Frontend**: Android (Kotlin) + Jetpack Compose
- **Backend**: Flask (Python) + MongoDB
- **Auth**: Firebase Phone OTP
- **Payments**: Razorpay
- **Real-time**: Socket.IO

## Quick Start

### Backend
```bash
cd backend
pip install -r requirements.txt
cp .env.example .env   # fill in your credentials
python run.py
```

### Android
1. Open `android/` in Android Studio
2. Add `google-services.json` from Firebase Console
3. Update `BASE_URL` in `AppModule.kt` to your backend IP
4. Run on emulator or device

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/verify-token | Firebase token → JWT |
| GET | /api/matches/?status=upcoming | List matches |
| POST | /api/matches/{id}/join | Join a match |
| GET | /api/leaderboard/ | Global leaderboard |
| POST | /api/rewards/daily-claim | Claim daily reward |
| POST | /api/rewards/spin | Spin the wheel |
| POST | /api/wallet/deposit/create-order | Razorpay order |
| GET | /api/wallet/transactions | Tx history |
| POST | /api/admin/matches/{id}/distribute-prizes | Admin: pay winners |

## Features
- OTP login via Firebase
- Match creation, joining, result submission
- Wallet with Razorpay deposits/withdrawals
- Daily login rewards + 7-day streak
- Spin-to-win wheel
- Achievements & XP leveling
- Rank tiers: Bronze → Legend
- Real-time leaderboard
- In-match chat (Socket.IO)
- Push notifications (FCM)
- Admin panel for match management
- Report system + trust score
- Referral system (₹50 bonus)
