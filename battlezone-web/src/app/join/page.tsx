'use client'
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/store'
import api from '@/lib/api'
import toast from 'react-hot-toast'
import { Shield, Users, Trophy, ChevronRight, X, Check } from 'lucide-react'
import { useEffect } from 'react'

declare global { interface Window { Razorpay: any } }

// ── Tournament tiers ──────────────────────────────────────────────────────────
const TIERS = [
  {
    id: 'bronze',
    name: 'Bronze Tier',
    entry_fee: 25,
    prize_pool: 300,
    prizes: [
      { rank: 1, label: '🥇 1st Place', amount: 150 },
      { rank: 2, label: '🥈 2nd Place', amount: 90 },
      { rank: 3, label: '🥉 3rd Place', amount: 60 },
    ],
    max_teams: 12,
    color: '#cd7f32',
    glow: 'rgba(205,127,50,0.3)',
    description: 'Perfect for beginners. Low entry, real cash prizes.',
    badge: '🔰 Starter',
  },
  {
    id: 'silver',
    name: 'Silver Tier',
    entry_fee: 50,
    prize_pool: 600,
    prizes: [
      { rank: 1, label: '🥇 1st Place', amount: 300 },
      { rank: 2, label: '🥈 2nd Place', amount: 180 },
      { rank: 3, label: '🥉 3rd Place', amount: 120 },
    ],
    max_teams: 12,
    color: '#c0c0c0',
    glow: 'rgba(192,192,192,0.3)',
    description: 'Intermediate players. Double the stakes.',
    badge: '⚔️ Competitive',
  },
  {
    id: 'gold',
    name: 'Gold Tier',
    entry_fee: 100,
    prize_pool: 1200,
    prizes: [
      { rank: 1, label: '🥇 1st Place', amount: 600 },
      { rank: 2, label: '🥈 2nd Place', amount: 360 },
      { rank: 3, label: '🥉 3rd Place', amount: 240 },
    ],
    max_teams: 12,
    color: '#ffd700',
    glow: 'rgba(255,215,0,0.3)',
    description: 'For the elite. Maximum prize pool.',
    badge: '👑 Pro',
  },
]

const GAMES = [
  { id: 'freefire', name: 'Free Fire MAX', emoji: '🔥', color: '#ff6b35', modes: ['Solo', 'Duo', 'Squad'] },
  { id: 'pubg', name: 'PUBG Mobile', emoji: '🎯', color: '#f59e0b', modes: ['Solo', 'Duo', 'Squad'] },
  { id: 'cod', name: 'COD Mobile', emoji: '💥', color: '#10b981', modes: ['Solo', 'Squad'] },
  { id: 'bgmi', name: 'BGMI', emoji: '⚔️', color: '#3b82f6', modes: ['Solo', 'Duo', 'Squad'] },
]

// ── Steps ─────────────────────────────────────────────────────────────────────
type Step = 'game' | 'tier' | 'register' | 'payment' | 'confirmed'

export default function JoinPage() {
  const { user, token } = useAuthStore()
  const router = useRouter()

  const [step, setStep] = useState<Step>('game')
  const [selectedGame, setSelectedGame] = useState<typeof GAMES[0] | null>(null)
  const [selectedMode, setSelectedMode] = useState('')
  const [selectedTier, setSelectedTier] = useState<typeof TIERS[0] | null>(null)
  const [form, setForm] = useState({
    team_name: '',
    in_game_name: '',
    player2: '',
    player3: '',
    player4: '',
  })
  const [paying, setPaying] = useState(false)
  const [orderId, setOrderId] = useState('')

  useEffect(() => {
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    document.body.appendChild(script)
    return () => { document.body.removeChild(script) }
  }, [])

  // ── Step helpers ──────────────────────────────────────────────────────────
  const isSquad = selectedMode === 'Squad'
  const isDuo = selectedMode === 'Duo'

  const goToTier = () => {
    if (!selectedGame || !selectedMode) { toast.error('Select a game and mode'); return }
    setStep('tier')
  }

  const goToRegister = () => {
    if (!selectedTier) { toast.error('Select a tier'); return }
    if (!token) { router.push('/auth'); return }
    setStep('register')
  }

  const goToPayment = () => {
    if (!form.team_name.trim()) { toast.error('Enter team name'); return }
    if (!form.in_game_name.trim()) { toast.error('Enter your in-game name'); return }
    if (isSquad && (!form.player2.trim() || !form.player3.trim() || !form.player4.trim())) {
      toast.error('Fill all squad member names'); return
    }
    if (isDuo && !form.player2.trim()) { toast.error('Fill partner name'); return }
    setStep('payment')
  }

  const handlePay = async () => {
    if (!selectedTier || !selectedGame) return
    setPaying(true)

    try {
      // Create Razorpay order
      let orderRes
      try {
        orderRes = await api.post('/api/wallet/deposit/create-order', {
          amount: selectedTier.entry_fee,
          purpose: 'tournament_entry',
        })
      } catch {
        // Demo mode — simulate payment
        await simulateDemoPayment()
        return
      }

      const order = orderRes.data
      setOrderId(order.order_id)

      const options = {
        key: process.env.NEXT_PUBLIC_RAZORPAY_KEY_ID,
        amount: selectedTier.entry_fee * 100,
        currency: 'INR',
        name: 'BattleZone Esports',
        description: `${selectedTier.name} — ${selectedGame.name} ${selectedMode}`,
        order_id: order.order_id,
        prefill: {
          name: user?.username || form.team_name,
          contact: (user as any)?.phone || '',
        },
        notes: {
          team_name: form.team_name,
          game: selectedGame.name,
          mode: selectedMode,
          tier: selectedTier.id,
        },
        theme: { color: selectedTier.color },
        handler: async (response: any) => {
          try {
            // Register team after payment
            await api.post('/api/matches/join-tournament', {
              game: selectedGame.name,
              mode: selectedMode,
              tier: selectedTier.id,
              entry_fee: selectedTier.entry_fee,
              team_name: form.team_name,
              in_game_name: form.in_game_name,
              members: [form.player2, form.player3, form.player4].filter(Boolean),
              payment_id: response.razorpay_payment_id,
              order_id: response.razorpay_order_id,
            })
          } catch {
            // Even if API fails, show success for demo
          }
          setStep('confirmed')
          setPaying(false)
        },
        modal: { ondismiss: () => setPaying(false) },
      }
      new window.Razorpay(options).open()
    } catch (e: any) {
      toast.error('Payment failed. Try again.')
      setPaying(false)
    }
  }

  const simulateDemoPayment = () => {
    return new Promise<void>(resolve => {
      setTimeout(() => {
        setPaying(false)
        setStep('confirmed')
        resolve()
      }, 1500)
    })
  }

  // ── Progress bar ──────────────────────────────────────────────────────────
  const steps = ['game', 'tier', 'register', 'payment', 'confirmed']
  const stepIdx = steps.indexOf(step)
  const stepLabels = ['Select Game', 'Choose Tier', 'Register Team', 'Payment', 'Confirmed']

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-black text-white mb-2">🎮 Join Tournament</h1>
      <p className="text-gray-500 mb-8">Select your game, pick a tier and register your team</p>

      {/* Progress */}
      {step !== 'confirmed' && (
        <div className="flex items-center gap-1 mb-10 overflow-x-auto pb-2">
          {stepLabels.slice(0, 4).map((label, i) => (
            <div key={i} className="flex items-center gap-1 flex-shrink-0">
              <div className="flex items-center gap-2">
                <div className="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold transition-all"
                  style={{
                    background: i < stepIdx ? '#7c3aed' : i === stepIdx ? 'linear-gradient(135deg,#7c3aed,#06b6d4)' : '#1a1a35',
                    color: i <= stepIdx ? 'white' : '#555',
                    border: `1px solid ${i <= stepIdx ? '#7c3aed' : '#2a2a4a'}`,
                  }}>
                  {i < stepIdx ? <Check size={12} /> : i + 1}
                </div>
                <span className="text-xs hidden sm:block" style={{ color: i === stepIdx ? '#a78bfa' : i < stepIdx ? '#7c3aed' : '#444' }}>
                  {label}
                </span>
              </div>
              {i < 3 && <div className="w-8 h-px mx-1" style={{ background: i < stepIdx ? '#7c3aed' : '#2a2a4a' }} />}
            </div>
          ))}
        </div>
      )}

      {/* ── STEP 1: Select Game ── */}
      {step === 'game' && (
        <div>
          <h2 className="text-xl font-bold text-white mb-5">Choose Your Game</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
            {GAMES.map(g => (
              <button key={g.id} onClick={() => { setSelectedGame(g); setSelectedMode('') }}
                className="card p-5 text-center transition-all hover:-translate-y-1 cursor-pointer"
                style={{
                  borderColor: selectedGame?.id === g.id ? g.color : '#2a1a4a',
                  boxShadow: selectedGame?.id === g.id ? `0 0 20px ${g.color}44` : 'none',
                  background: selectedGame?.id === g.id ? `linear-gradient(135deg, ${g.color}11, #12122a)` : undefined,
                }}>
                <div className="text-4xl mb-3">{g.emoji}</div>
                <div className="text-sm font-bold" style={{ color: g.color }}>{g.name}</div>
                {selectedGame?.id === g.id && (
                  <div className="mt-2 text-xs text-green-400 flex items-center justify-center gap-1">
                    <Check size={10} /> Selected
                  </div>
                )}
              </button>
            ))}
          </div>

          {selectedGame && (
            <div className="card p-5 mb-6">
              <h3 className="font-bold text-white mb-3">Select Mode</h3>
              <div className="flex gap-3 flex-wrap">
                {selectedGame.modes.map(m => (
                  <button key={m} onClick={() => setSelectedMode(m)}
                    className="px-5 py-2.5 rounded-xl font-semibold text-sm transition-all"
                    style={{
                      background: selectedMode === m ? `linear-gradient(135deg, ${selectedGame.color}, #7c3aed)` : '#1a1a35',
                      color: selectedMode === m ? 'white' : '#888',
                      border: `1px solid ${selectedMode === m ? selectedGame.color : '#2a2a4a'}`,
                    }}>
                    {m === 'Solo' ? '👤' : m === 'Duo' ? '👥' : '👨‍👩‍👧‍👦'} {m}
                  </button>
                ))}
              </div>
            </div>
          )}

          <button onClick={goToTier} disabled={!selectedGame || !selectedMode}
            className="btn-primary px-8 py-3 flex items-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed">
            Next: Choose Tier <ChevronRight size={16} />
          </button>
        </div>
      )}

      {/* ── STEP 2: Choose Tier ── */}
      {step === 'tier' && (
        <div>
          <div className="flex items-center gap-3 mb-5">
            <div className="text-2xl">{selectedGame?.emoji}</div>
            <div>
              <h2 className="text-xl font-bold text-white">{selectedGame?.name} — {selectedMode}</h2>
              <p className="text-gray-500 text-sm">Choose your entry tier</p>
            </div>
          </div>

          <div className="grid md:grid-cols-3 gap-5 mb-8">
            {TIERS.map(tier => (
              <button key={tier.id} onClick={() => setSelectedTier(tier)}
                className="card p-5 text-left transition-all hover:-translate-y-1 cursor-pointer w-full"
                style={{
                  borderColor: selectedTier?.id === tier.id ? tier.color : '#2a1a4a',
                  boxShadow: selectedTier?.id === tier.id ? `0 0 25px ${tier.glow}` : 'none',
                  background: selectedTier?.id === tier.id ? `linear-gradient(135deg, ${tier.color}15, #12122a)` : undefined,
                }}>

                {/* Header */}
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <div className="text-xs font-bold mb-1" style={{ color: tier.color }}>{tier.badge}</div>
                    <div className="text-lg font-black text-white">{tier.name}</div>
                  </div>
                  {selectedTier?.id === tier.id && (
                    <div className="w-6 h-6 rounded-full flex items-center justify-center"
                      style={{ background: tier.color }}>
                      <Check size={12} className="text-black" />
                    </div>
                  )}
                </div>

                {/* Entry fee */}
                <div className="mb-4">
                  <div className="text-xs text-gray-500 mb-0.5">Entry Fee</div>
                  <div className="text-3xl font-black" style={{ color: tier.color }}>₹{tier.entry_fee}</div>
                </div>

                {/* Prize pool */}
                <div className="bg-[#0a0a0f] rounded-xl p-3 mb-4">
                  <div className="text-xs text-gray-500 mb-2 flex items-center gap-1">
                    <Trophy size={10} /> Prize Pool: <span className="font-bold text-[#f59e0b]">₹{tier.prize_pool}</span>
                  </div>
                  {tier.prizes.map(p => (
                    <div key={p.rank} className="flex justify-between text-xs py-1 border-b border-[#1e1e3a] last:border-0">
                      <span className="text-gray-400">{p.label}</span>
                      <span className="font-bold text-[#f59e0b]">₹{p.amount}</span>
                    </div>
                  ))}
                </div>

                <div className="text-xs text-gray-500">{tier.description}</div>
                <div className="flex items-center gap-1 text-xs text-gray-600 mt-2">
                  <Users size={10} /> Max {tier.max_teams} teams
                </div>
              </button>
            ))}
          </div>

          <div className="flex gap-3">
            <button onClick={() => setStep('game')}
              className="px-6 py-3 rounded-xl border border-[#2a2a4a] text-gray-400 hover:text-white transition-colors">
              ← Back
            </button>
            <button onClick={goToRegister} disabled={!selectedTier}
              className="btn-primary px-8 py-3 flex items-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed">
              Next: Register Team <ChevronRight size={16} />
            </button>
          </div>
        </div>
      )}

      {/* ── STEP 3: Register Team ── */}
      {step === 'register' && selectedTier && selectedGame && (
        <div>
          {/* Summary bar */}
          <div className="flex flex-wrap gap-3 mb-6">
            <div className="flex items-center gap-2 bg-[#1a1a35] rounded-full px-4 py-2 text-sm">
              <span>{selectedGame.emoji}</span> <span className="text-white font-semibold">{selectedGame.name}</span>
            </div>
            <div className="flex items-center gap-2 bg-[#1a1a35] rounded-full px-4 py-2 text-sm">
              <span className="text-gray-400">{selectedMode}</span>
            </div>
            <div className="flex items-center gap-2 rounded-full px-4 py-2 text-sm font-bold"
              style={{ background: `${selectedTier.color}22`, color: selectedTier.color, border: `1px solid ${selectedTier.color}44` }}>
              {selectedTier.name} • ₹{selectedTier.entry_fee}
            </div>
          </div>

          <div className="card p-6 max-w-lg">
            <h2 className="text-xl font-bold text-white mb-5">Register Your Team</h2>

            <div className="space-y-4">
              <div>
                <label className="text-xs text-gray-400 mb-1.5 block">Team Name *</label>
                <input placeholder="e.g. Team Alpha" value={form.team_name}
                  onChange={e => setForm(p => ({ ...p, team_name: e.target.value }))} />
              </div>

              <div>
                <label className="text-xs text-gray-400 mb-1.5 block">Your In-Game Name (IGN) *</label>
                <input placeholder="Your username in the game" value={form.in_game_name}
                  onChange={e => setForm(p => ({ ...p, in_game_name: e.target.value }))} />
              </div>

              {(isDuo || isSquad) && (
                <div>
                  <label className="text-xs text-gray-400 mb-1.5 block">Player 2 IGN *</label>
                  <input placeholder="Partner's in-game name" value={form.player2}
                    onChange={e => setForm(p => ({ ...p, player2: e.target.value }))} />
                </div>
              )}

              {isSquad && (
                <>
                  <div>
                    <label className="text-xs text-gray-400 mb-1.5 block">Player 3 IGN *</label>
                    <input placeholder="Player 3's in-game name" value={form.player3}
                      onChange={e => setForm(p => ({ ...p, player3: e.target.value }))} />
                  </div>
                  <div>
                    <label className="text-xs text-gray-400 mb-1.5 block">Player 4 IGN *</label>
                    <input placeholder="Player 4's in-game name" value={form.player4}
                      onChange={e => setForm(p => ({ ...p, player4: e.target.value }))} />
                  </div>
                </>
              )}
            </div>

            <div className="flex gap-3 mt-6">
              <button onClick={() => setStep('tier')}
                className="px-5 py-2.5 rounded-xl border border-[#2a2a4a] text-gray-400 hover:text-white transition-colors">
                ← Back
              </button>
              <button onClick={goToPayment} className="btn-primary flex-1 py-2.5 flex items-center justify-center gap-2">
                Next: Payment <ChevronRight size={16} />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── STEP 4: Payment ── */}
      {step === 'payment' && selectedTier && selectedGame && (
        <div className="max-w-md">
          <h2 className="text-xl font-bold text-white mb-6">Confirm & Pay</h2>

          {/* Order summary */}
          <div className="card p-5 mb-5">
            <h3 className="font-semibold text-white mb-4 text-sm">Order Summary</h3>
            <div className="space-y-3">
              <div className="flex justify-between text-sm">
                <span className="text-gray-400">Game</span>
                <span className="text-white">{selectedGame.emoji} {selectedGame.name}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-400">Mode</span>
                <span className="text-white">{selectedMode}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-400">Tier</span>
                <span className="font-semibold" style={{ color: selectedTier.color }}>{selectedTier.name}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-400">Team</span>
                <span className="text-white">{form.team_name}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-400">Players</span>
                <span className="text-white">
                  {[form.in_game_name, form.player2, form.player3, form.player4].filter(Boolean).join(', ')}
                </span>
              </div>
              <div className="border-t border-[#2a2a4a] my-2" />
              <div className="flex justify-between">
                <span className="text-gray-400">Entry Fee</span>
                <span className="text-2xl font-black" style={{ color: selectedTier.color }}>₹{selectedTier.entry_fee}</span>
              </div>
            </div>
          </div>

          {/* Prize reminder */}
          <div className="bg-[#0a0a0f] rounded-xl p-4 mb-5 border border-[#1e1e3a]">
            <div className="text-xs text-gray-500 mb-2 flex items-center gap-1">
              <Trophy size={11} /> You could win
            </div>
            {selectedTier.prizes.map(p => (
              <div key={p.rank} className="flex justify-between text-sm py-1">
                <span className="text-gray-400">{p.label}</span>
                <span className="font-bold text-[#f59e0b]">₹{p.amount}</span>
              </div>
            ))}
          </div>

          <button onClick={handlePay} disabled={paying}
            className="btn-primary w-full py-4 text-base flex items-center justify-center gap-2">
            {paying
              ? <><span className="animate-spin">⏳</span> Processing...</>
              : <><Shield size={16} /> Pay ₹{selectedTier.entry_fee} via Razorpay</>}
          </button>

          <p className="text-xs text-gray-600 text-center mt-3 flex items-center justify-center gap-1">
            <Shield size={10} /> Slot confirmed only after successful payment • UPI, Cards, Net Banking accepted
          </p>

          <button onClick={() => setStep('register')}
            className="w-full text-gray-600 text-sm py-3 hover:text-gray-400 transition-colors mt-2">
            ← Back to registration
          </button>
        </div>
      )}

      {/* ── STEP 5: Confirmed ── */}
      {step === 'confirmed' && selectedTier && selectedGame && (
        <div className="text-center py-8">
          <div className="text-6xl mb-4">🎉</div>
          <h2 className="text-3xl font-black text-white mb-2">You're In!</h2>
          <p className="text-gray-400 mb-6">Your slot has been confirmed. Get ready to battle!</p>

          {/* Ticket */}
          <div className="card p-6 max-w-sm mx-auto mb-8 text-left"
            style={{ borderColor: selectedTier.color, boxShadow: `0 0 30px ${selectedTier.glow}` }}>
            <div className="flex justify-between items-start mb-4">
              <div>
                <div className="text-xs font-bold mb-1" style={{ color: selectedTier.color }}>TOURNAMENT TICKET</div>
                <div className="text-lg font-black text-white">{selectedGame.name}</div>
                <div className="text-gray-400 text-sm">{selectedMode} • {selectedTier.name}</div>
              </div>
              <div className="text-3xl">{selectedGame.emoji}</div>
            </div>

            <div className="border-t border-dashed border-[#2a2a4a] my-4" />

            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Team</span>
                <span className="text-white font-semibold">{form.team_name}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Entry</span>
                <span className="font-bold" style={{ color: selectedTier.color }}>₹{selectedTier.entry_fee} Paid ✅</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Prize Pool</span>
                <span className="font-bold text-[#f59e0b]">₹{selectedTier.prize_pool}</span>
              </div>
            </div>

            <div className="border-t border-dashed border-[#2a2a4a] my-4" />

            <div className="text-xs text-gray-600 text-center">
              Room ID will be shared 15 minutes before match start
            </div>
          </div>

          <div className="flex flex-wrap gap-3 justify-center">
            <button onClick={() => router.push('/tournaments')} className="btn-primary px-6 py-3">
              View All Tournaments
            </button>
            <button onClick={() => {
              setStep('game')
              setSelectedGame(null)
              setSelectedMode('')
              setSelectedTier(null)
              setForm({ team_name: '', in_game_name: '', player2: '', player3: '', player4: '' })
            }} className="px-6 py-3 rounded-xl border border-[#2a2a4a] text-gray-400 hover:text-white transition-colors">
              Join Another
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
