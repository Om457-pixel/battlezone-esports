'use client'
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import api from '@/lib/api'
import { useAuthStore } from '@/lib/store'
import toast from 'react-hot-toast'

export default function AuthPage() {
  const router = useRouter()
  const { setToken, setUser } = useAuthStore()
  const [step, setStep] = useState<'phone' | 'otp' | 'username'>('phone')
  const [phone, setPhone] = useState('')
  const [otp, setOtp] = useState('')
  const [username, setUsername] = useState('')
  const [loading, setLoading] = useState(false)
  const [isNew, setIsNew] = useState(false)

  // For demo — simulate OTP flow
  const sendOtp = async () => {
    if (phone.length < 10) { toast.error('Enter valid phone number'); return }
    setLoading(true)
    try {
      // In real app: Firebase phone auth sends OTP
      // For demo, we simulate
      toast.success('OTP sent to ' + phone)
      setStep('otp')
    } catch (e) {
      toast.error('Failed to send OTP')
    } finally {
      setLoading(false)
    }
  }

  const verifyOtp = async () => {
    if (otp.length < 4) { toast.error('Enter OTP'); return }
    setLoading(true)
    try {
      // In real app: verify Firebase OTP, get idToken
      // Then POST /api/auth/verify-token
      // For demo, simulate login
      const mockToken = 'demo_jwt_token_' + Date.now()
      setToken(mockToken)
      setUser({
        uid: 'demo_' + phone,
        username: username || 'Player_' + phone.slice(-4),
        phone,
        wallet_balance: 100,
        bonus_balance: 50,
        level: 1,
        rank_tier: 'Bronze',
        stats: { matches_played: 0, matches_won: 0, total_kills: 0, win_rate: 0, rank_points: 0 }
      })
      toast.success('Welcome to BattleZone! 🎮')
      router.push('/')
    } catch (e: any) {
      if (e.response?.status === 404) {
        setIsNew(true)
        setStep('username')
      } else {
        toast.error('Invalid OTP')
      }
    } finally {
      setLoading(false)
    }
  }

  const createAccount = async () => {
    if (username.length < 3) { toast.error('Username must be 3+ characters'); return }
    setLoading(true)
    try {
      const res = await api.post('/api/auth/register', { phone, username, firebase_token: 'demo' })
      setToken(res.data.token)
      setUser(res.data.user)
      toast.success('Account created! Welcome 🎮')
      router.push('/')
    } catch (e: any) {
      toast.error(e.response?.data?.error || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-4"
      style={{ background: 'radial-gradient(ellipse at 50% 0%, rgba(124,58,237,0.1) 0%, transparent 70%)' }}>
      <div className="card p-8 w-full max-w-sm">
        <div className="text-center mb-8">
          <div className="text-4xl font-black neon-text mb-2">⚡ BattleZone</div>
          <p className="text-gray-500 text-sm">Sign in to join tournaments & win prizes</p>
        </div>

        {step === 'phone' && (
          <div className="space-y-4">
            <div>
              <label className="text-xs text-gray-400 mb-1.5 block">Phone Number</label>
              <div className="flex gap-2">
                <div className="bg-[#1a1a35] border border-[#2a2a4a] rounded-xl px-3 py-2.5 text-sm text-gray-400 whitespace-nowrap">🇮🇳 +91</div>
                <input placeholder="10-digit number" value={phone}
                  onChange={e => setPhone(e.target.value.replace(/\D/g, '').slice(0, 10))}
                  className="flex-1"
                />
              </div>
            </div>
            <button onClick={sendOtp} disabled={loading} className="btn-primary w-full py-3">
              {loading ? 'Sending...' : 'Send OTP →'}
            </button>
          </div>
        )}

        {step === 'otp' && (
          <div className="space-y-4">
            <p className="text-sm text-gray-400 text-center">OTP sent to +91 {phone}</p>
            <div>
              <label className="text-xs text-gray-400 mb-1.5 block">Enter OTP</label>
              <input placeholder="6-digit OTP" value={otp} maxLength={6}
                onChange={e => setOtp(e.target.value.replace(/\D/g, ''))}
                className="text-center text-2xl tracking-widest"
              />
            </div>
            <button onClick={verifyOtp} disabled={loading} className="btn-primary w-full py-3">
              {loading ? 'Verifying...' : 'Verify & Login'}
            </button>
            <button onClick={() => setStep('phone')} className="w-full text-gray-500 text-sm py-2 hover:text-gray-300">
              ← Change number
            </button>
          </div>
        )}

        {step === 'username' && (
          <div className="space-y-4">
            <p className="text-sm text-gray-400 text-center">Choose your gamer tag</p>
            <div>
              <label className="text-xs text-gray-400 mb-1.5 block">Username</label>
              <input placeholder="e.g. SnipeKing99" value={username}
                onChange={e => setUsername(e.target.value)}
              />
            </div>
            <button onClick={createAccount} disabled={loading} className="btn-primary w-full py-3">
              {loading ? 'Creating...' : 'Create Account 🎮'}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
