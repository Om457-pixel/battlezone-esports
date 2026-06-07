# Database Schema (MongoDB)

## Collections

### users
```json
{
  "uid": "firebase_uid",
  "username": "GamerPro123",
  "phone": "+919876543210",
  "email": "user@example.com",
  "avatar": "https://...",
  "bio": "Pro gamer",
  "level": 5,
  "xp": 450,
  "xp_to_next_level": 750,
  "wallet_balance": 250.0,
  "bonus_balance": 50.0,
  "total_earnings": 1500.0,
  "referral_code": "GAMER123",
  "referred_by": "FRIEND456",
  "stats": {
    "matches_played": 25,
    "matches_won": 8,
    "total_kills": 120,
    "win_rate": 32.0,
    "rank_points": 2400
  },
  "rank_tier": "Gold",
  "trust_score": 95,
  "achievements": [{"id": "first_win", "earned_at": "..."}],
  "badges": [],
  "streak": 5,
  "last_login": "2024-01-15T10:00:00Z",
  "last_daily_reward": "2024-01-15T10:00:00Z",
  "spin_available": false,
  "is_banned": false,
  "is_admin": false,
  "notification_prefs": {...}
}
```

### matches
```json
{
  "match_id": "uuid",
  "title": "Free Fire Friday Cup",
  "game": "Free Fire MAX",
  "mode": "Squad",
  "map": "Bermuda",
  "entry_fee": 20.0,
  "prize_pool": 500.0,
  "prize_distribution": [
    {"rank": 1, "prize": 250.0},
    {"rank": 2, "prize": 150.0},
    {"rank": 3, "prize": 100.0}
  ],
  "max_players": 100,
  "current_players": 45,
  "start_time": "2024-01-16T18:00:00Z",
  "status": "upcoming",
  "room_id": null,
  "room_password": null,
  "banner_url": "https://...",
  "rules": "No hacking...",
  "created_by": "admin_uid",
  "is_featured": true
}
```

### joined_players
```json
{
  "match_id": "uuid",
  "user_id": "firebase_uid",
  "in_game_name": "ProSniper99",
  "team_name": "Team Alpha",
  "slot": 1,
  "kills": 8,
  "rank": 2,
  "prize_won": 150.0,
  "result_screenshot": "https://...",
  "joined_at": "2024-01-15T12:00:00Z"
}
```

### transactions
```json
{
  "tx_id": "uuid",
  "user_id": "firebase_uid",
  "type": "prize_won",
  "amount": 250.0,
  "description": "Prize for rank #1 in Free Fire Friday Cup",
  "reference_id": "match_uuid",
  "status": "completed",
  "created_at": "2024-01-16T20:00:00Z"
}
```

### reports
```json
{
  "reporter_id": "firebase_uid",
  "reported_user": "username",
  "match_id": "uuid",
  "reason": "cheating",
  "description": "Using aimbot",
  "status": "pending",
  "created_at": "..."
}
```

### notifications
```json
{
  "notif_id": "uuid",
  "user_id": "firebase_uid",
  "title": "Match Starting Soon!",
  "body": "Your match starts in 15 minutes",
  "type": "match_reminder",
  "data": {"match_id": "uuid"},
  "read": false,
  "created_at": "..."
}
```

### chat_messages
```json
{
  "match_id": "uuid",
  "user_id": "firebase_uid",
  "username": "GamerPro",
  "avatar": "https://...",
  "level": 5,
  "message": "GG everyone!",
  "timestamp": "..."
}
```

### withdrawals
```json
{
  "user_id": "firebase_uid",
  "amount": 500.0,
  "upi_id": "user@upi",
  "status": "pending",
  "created_at": "..."
}
```
