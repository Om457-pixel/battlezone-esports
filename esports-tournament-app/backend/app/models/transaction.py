from datetime import datetime
from ..database import get_db
import uuid

TX_TYPES = ["deposit", "withdrawal", "entry_fee", "prize_won", "referral_bonus", "daily_reward", "spin_reward", "refund"]

def create_transaction(user_id, tx_type, amount, description, reference_id=None):
    db = get_db()
    tx = {
        "tx_id": str(uuid.uuid4()),
        "user_id": user_id,
        "type": tx_type,
        "amount": float(amount),
        "description": description,
        "reference_id": reference_id,
        "status": "completed",
        "created_at": datetime.utcnow()
    }
    db.transactions.insert_one(tx)
    return tx

def debit_wallet(user_id, amount, tx_type, description, reference_id=None):
    db = get_db()
    user = db.users.find_one({"uid": user_id})
    if not user or user['wallet_balance'] < amount:
        return {"error": "Insufficient balance"}
    db.users.update_one({"uid": user_id}, {"$inc": {"wallet_balance": -amount}})
    return create_transaction(user_id, tx_type, -amount, description, reference_id)

def credit_wallet(user_id, amount, tx_type, description, reference_id=None):
    db = get_db()
    db.users.update_one({"uid": user_id}, {
        "$inc": {"wallet_balance": amount, "total_earnings": amount if tx_type == "prize_won" else 0}
    })
    return create_transaction(user_id, tx_type, amount, description, reference_id)

def get_transaction_history(user_id, limit=20, skip=0):
    db = get_db()
    return list(db.transactions.find(
        {"user_id": user_id}, {"_id": 0}
    ).sort("created_at", -1).skip(skip).limit(limit))
