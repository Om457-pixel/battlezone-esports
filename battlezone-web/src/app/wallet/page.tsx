'use client'
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import api from '@/lib/api'
import { useAuthStore } from '@/lib/store'
import toast from 'react-hot-toast'
import { ArrowDownLeft, ArrowUpRight, Plus, Wallet } from 'lucide-react'

declare global { interface Window { Razorpay: any } }

export default function WalletPage() {
  const { user, token, setUser } = useAuthStore()
  const router = useRouter()
  const [transactions, setTransactions] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [depositAmount, setDepositAmount] = useState('')
  const [depositing, setDepositing] = useState(false)
  const [tab, setTab] = useState<'all'|'deposit'|'withdrawal'|'prize'>('all')

  useEffect(() => {
    if (!token) { router.push('/auth'); return }
    api.get('/api/users/me').then(r => setUser(r.data)).catch(() => {})
    api.get('/api/wallet/transactions')
      .then(r => setTransactions(r.data.transactions || []))
      .catch(() => setTransactions(DEMO_TXN))
      .finally(() => setLoading(false))
  }, [token])

  useEffect(() => {
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    document.body.appendChild(script)
    return () => { document.body.removeChild(script) }
  }, [])

  const handleDeposit = async () => {
    const amount = parseFloat(depositAmount)
    if (!amount || amount < 10) { toast.error('Minimum deposit ₹10'); return }
    setDepositing(true)
    try {
      const res = await api.post('/api/wallet/deposit/create-order', { amount })
      const order = res.data
      const options = {
        key: process.env.NEXT_PUBLIC_RAZORPAY_KEY_ID,
        amount: order.amount,
        currency: 'INR',
        name: 'BattleZone',
        description: 'Wallet Deposit',
        order_id: order.order_id,
        theme: { color: '#7c3aed' },
        handler: async (response: any) => {
          try {
            await api.post('/api/wallet/deposit/verify', {
              order_id: order.order_id,
              payment_id: response.razorpay_payment_id,
              signature: response.razorpay_signature,
              amount,
            })
            toast.success(`₹${amount} added to wallet!`)
            api.get('/api/users/me').then(r => setUser(r.data)).catch(() => {})
            api.get('/api/wallet/transactions').then(r => setTransactions(r.data.transactions || [])).catch(() => {})
            setDepositAmount('')
          } catch { toast.error('Payment verification failed') }
        },
        modal: { ondismiss: () => setDepositing(false) },
      }
      new window.Razorpay(options).open()
    } catch (e: any) {
      toast.error(e.response?.data?.error || 'Failed to create order')
      setDepositing(false)
    }
  }

  const filtered = tab === 'all' ? transactions : transactions.filter(t => t.type === tab)

  const txnIcon = (type: string) => {
    if (['deposit','prize','refund','referral','reward'].includes(type)) return <ArrowDownLeft size={16} className="text-green-400" />
    return <ArrowUpRight size={16} className="text-red-400" />
  }

  const txnColor = (type: string) => ['deposit','prize','refund','referral','reward'].includes(type) ? '#10b981' : '#ef4444'
  const txnSign = (type: string) => ['deposit','prize','refund','referral','reward'].includes(type) ? '+' : '-'

  if (!user) return null

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-black text-white mb-6">💰 Wallet</h1>

      {/* Balance cards */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="card p-5" style={{ background: 'linear-gradient(135deg,#1a0a2e,#0e1a2e)', borderColor: '#3b1a6a' }}>
          <div className="flex items-center gap-2 text-gray-500 text-sm mb-2">
            <Wallet size={14} /> Main Balance
          </div>
          <div className="text-4xl font-black text-[#f59e0b]">₹{user.wallet_balance?.toFixed(2)}</div>
          <div className="text-xs text-gray-600 mt-1">Withdrawable</div>
        </div>
        <div className="card p-5">
          <div className="flex items-center gap-2 text-gray-500 text-sm mb-2">
            <Plus size={14} /> Bonus Balance
          </div>
          <div className="text-4xl font-black text-purple-400">₹{user.bonus_balance?.toFixed(2)}</div>
          <div className="text-xs text-gray-600 mt-1">Use for entry fees</div>
        </div>
      </div>

      {/* Quick deposit */}
      <div className="card p-5 mb-6">
        <h2 className="font-bold text-white mb-4">⚡ Add Money</h2>

        {/* Quick amounts */}
        <div className="flex gap-2 flex-wrap mb-3">
          {[50, 100, 200, 500, 1000].map(amt => (
            <button key={amt} onClick={() => setDepositAmount(String(amt))}
              className="px-3 py-1.5 rounded-lg text-sm transition-all"
              style={{
                background: depositAmount === String(amt) ? '#7c3aed' : '#1a1a35',
                color: depositAmount === String(amt) ? 'white' : '#888',
                border: `1px solid ${depositAmount === String(amt) ? '#7c3aed' : '#2a2a4a'}`,
              }}>
              ₹{amt}
            </button>
          ))}
        </div>

        <div className="flex gap-3">
          <input
            type="number"
            placeholder="Enter amount (min ₹10)"
            value={depositAmount}
            onChange={e => setDepositAmount(e.target.value)}
            className="flex-1"
            min={10}
          />
          <button onClick={handleDeposit} disabled={depositing} className="btn-primary px-6 whitespace-nowrap">
            {depositing ? '⏳' : 'Pay via Razorpay'}
          </button>
        </div>
        <p className="text-xs text-gray-600 mt-2">Powered by Razorpay • UPI, Cards, Net Banking</p>
      </div>

      {/* Transactions */}
      <div className="card p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-bold text-white">📋 Transaction History</h2>
          <div className="flex gap-1">
            {(['all','deposit','withdrawal','prize'] as const).map(t => (
              <button key={t} onClick={() => setTab(t)}
                className="px-2.5 py-1 rounded-lg text-xs capitalize transition-all"
                style={{
                  background: tab === t ? '#7c3aed' : '#1a1a35',
                  color: tab === t ? 'white' : '#666',
                }}>
                {t}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="space-y-2">{[1,2,3].map(i => <div key={i} className="h-14 bg-[#1a1a35] rounded-lg animate-pulse" />)}</div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-10 text-gray-600">
            <div className="text-3xl mb-2">💸</div>
            <div className="text-sm">No transactions yet</div>
          </div>
        ) : (
          <div className="space-y-2">
            {filtered.map((t: any, i: number) => (
              <div key={i} className="flex items-center gap-3 bg-[#1a1a35] rounded-xl px-4 py-3">
                <div className="w-8 h-8 rounded-lg bg-[#12122a] flex items-center justify-center">
                  {txnIcon(t.type)}
                </div>
                <div className="flex-1">
                  <div className="text-sm font-semibold text-white capitalize">{t.description || t.type}</div>
                  <div className="text-xs text-gray-500">{new Date(t.created_at || Date.now()).toLocaleString('en-IN')}</div>
                </div>
                <div className="text-right">
                  <div className="font-bold text-sm" style={{ color: txnColor(t.type) }}>
                    {txnSign(t.type)}₹{t.amount?.toFixed(2)}
                  </div>
                  <div className="text-xs text-gray-600 capitalize">{t.type}</div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

const DEMO_TXN = [
  { type: 'deposit', amount: 200, description: 'Wallet deposit via UPI', created_at: new Date(Date.now() - 86400000).toISOString() },
  { type: 'entry_fee', amount: 50, description: 'Entry fee - Friday Night Showdown', created_at: new Date(Date.now() - 72000000).toISOString() },
  { type: 'prize', amount: 2500, description: '🥇 1st place - Friday Night Showdown', created_at: new Date(Date.now() - 60000000).toISOString() },
  { type: 'entry_fee', amount: 100, description: 'Entry fee - PUBG Pro League', created_at: new Date(Date.now() - 36000000).toISOString() },
  { type: 'referral', amount: 50, description: 'Referral bonus - friend joined', created_at: new Date(Date.now() - 10000000).toISOString() },
]
