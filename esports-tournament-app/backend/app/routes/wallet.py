from flask import Blueprint, request, jsonify, current_app
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..database import get_db
from ..models.transaction import credit_wallet, debit_wallet, get_transaction_history
import razorpay
import uuid

wallet_bp = Blueprint('wallet', __name__)

def get_razorpay_client():
    return razorpay.Client(auth=(
        current_app.config['RAZORPAY_KEY_ID'],
        current_app.config['RAZORPAY_KEY_SECRET']
    ))

@wallet_bp.route('/balance', methods=['GET'])
@jwt_required()
def get_balance():
    uid = get_jwt_identity()
    db = get_db()
    user = db.users.find_one({"uid": uid}, {"wallet_balance": 1, "bonus_balance": 1, "_id": 0})
    return jsonify(user), 200

@wallet_bp.route('/deposit/create-order', methods=['POST'])
@jwt_required()
def create_deposit_order():
    uid = get_jwt_identity()
    data = request.get_json()
    amount = int(data.get('amount', 0))
    if amount < 10:
        return jsonify({"error": "Minimum deposit is ₹10"}), 400
    if amount > 10000:
        return jsonify({"error": "Maximum deposit is ₹10,000"}), 400

    try:
        client = get_razorpay_client()
        order = client.order.create({
            "amount": amount * 100,  # paise
            "currency": "INR",
            "receipt": str(uuid.uuid4()),
            "notes": {"user_id": uid}
        })
        return jsonify({"order_id": order['id'], "amount": amount, "currency": "INR"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@wallet_bp.route('/deposit/verify', methods=['POST'])
@jwt_required()
def verify_deposit():
    uid = get_jwt_identity()
    data = request.get_json()
    try:
        client = get_razorpay_client()
        client.utility.verify_payment_signature({
            'razorpay_order_id': data['order_id'],
            'razorpay_payment_id': data['payment_id'],
            'razorpay_signature': data['signature']
        })
        amount = data['amount']
        credit_wallet(uid, amount, "deposit", f"Wallet deposit via Razorpay", data['payment_id'])
        return jsonify({"success": True, "amount": amount}), 200
    except Exception as e:
        return jsonify({"error": "Payment verification failed"}), 400

@wallet_bp.route('/withdraw', methods=['POST'])
@jwt_required()
def withdraw():
    uid = get_jwt_identity()
    data = request.get_json()
    amount = float(data.get('amount', 0))
    if amount < 100:
        return jsonify({"error": "Minimum withdrawal is ₹100"}), 400

    db = get_db()
    user = db.users.find_one({"uid": uid})
    if not user or user['wallet_balance'] < amount:
        return jsonify({"error": "Insufficient balance"}), 400

    # Create pending withdrawal request
    db.withdrawals.insert_one({
        "user_id": uid,
        "amount": amount,
        "upi_id": data.get('upi_id'),
        "status": "pending",
        "created_at": __import__('datetime').datetime.utcnow()
    })
    debit_wallet(uid, amount, "withdrawal", f"Withdrawal request of ₹{amount}")
    return jsonify({"success": True, "message": "Withdrawal request submitted. Processing in 24-48 hours."}), 200

@wallet_bp.route('/transactions', methods=['GET'])
@jwt_required()
def transactions():
    uid = get_jwt_identity()
    limit = int(request.args.get('limit', 20))
    skip = int(request.args.get('skip', 0))
    txs = get_transaction_history(uid, limit, skip)
    for tx in txs:
        if hasattr(tx.get('created_at'), 'isoformat'):
            tx['created_at'] = tx['created_at'].isoformat()
    return jsonify({"transactions": txs}), 200
