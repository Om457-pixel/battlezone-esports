'use client'
import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import api from '@/lib/api'
import { useAuthStore } from '@/lib/store'
import toast from 'react-hot-toast'
import { Users, Trophy, Clock, Shield, ChevronRight, X } from 'lucide-react'

declare global {
  interface Window { Razorpay: any }
}

export default function TournamentDetail() {
  const { id } = useParams()
  const router = useRouter()
  const { user, token } = useAuthStore()
  const [tournament, setTournament] = useState<any>(null)
  const [players, setPlayers] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [showRegModal, setShowRegModal] = useState(false)
  const [regForm, setRegForm] = useState({ in_game_name: '', team_name: '' })
  const [paying, setPaying] = useState(false)

  useEffect(() => {
    Promise.all([
      api.get(`/api/matches/${id}`),
      api.get(`/api/matches/${id}/players`),
    ]).then(([tRes, pRes]) => {
      setTournament(tRes.data)
      setPlayers(pRes.data.players || [])
    }).catch(() => {
      setTournament(DEMO_TOURNAMENT)
      setPlayers(DEMO_PLAYERS)
    }).finally(() => setLoading(false))
  }, [id])

  // Load Razorpay script
  useEffect(() => {
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    script.async = true
    document.body.appendChild(script)
    return () => { document.body.removeChild(script) }
  }, [])

  const handleRegister = async () => {
    if (!token) { router.push('/auth'); return }
    if (!regForm.in_game_name.trim()) { toast.error('Enter your in-game name'); return }

    setPaying(true)
    try {
      if (tournament.entry_fee > 0) {
        // Step 1: Create Razorpay order
        const orderRes = await api.post('/api/wallet/deposit/create-order', {
          amount: tournament.entry_fee,
          purpose: 'entry_fee',
          match_id: id,
        })
        const order = orderRes.data

        // Step 2: Open Razorpay checkout
        const options = {
          key: process.env.NEXT_PUBLIC_RAZORPAY_KEY_ID,
          amount: order.amount,
          currency: 'INR',
          name: 'BattleZone',
          description: `Entry fee for ${tournament.title}`,
          order_id: order.order_id,
          prefill: { name: user?.username, contact: user?.phone },
          theme: { color: '#7c3aed' },
          handler: async (response: any) => {
            // Step 3: Verify payment + join match
            try {
              await api.post(`/api/matches/${id}/join`, {
                in_game_name: regForm.in_game_name,
                team_name: regForm.team_name || undefined,
                payment_id: response.razorpay_payment_id,
              })
              toast.success('🎉 Slot confirmed! You\'re registered.')
              setShowRegModal(false)
              router.refresh()
            } catch (e: any) {
              toast.error(e.response?.data?.error || 'Registration failed')
            }
          },
          modal: { ondismiss: () => setPaying(false) },
        }
        const rzp = new window.Razorpay(options)
        rzp.open()
      } else {
        // Free tournament — join directly
        await api.post(`/api/matches/${id}/join`, {
          in_game_name: regForm.in_game_name,
          team_name: regForm.team_name || undefined,
        })
        toast.success('🎉 Slot confirmed! You\'re registered.')
        setShowRegModal(false)
      }
    } catch (e: any) {
      toast.error(e.response?.data?.error || 'Something went wrong')
    } finally {
      setPaying(false)
    }
  }

  if (loading) return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <div className="card p-6 animate-pulse h-64" />
    </div>
  )

  if (!tournament) return <div className="text-center py-20 text-gray-500">Tournament not found</div>

  const fillPct = Math.round((tournament.current_players / tournament.max_players) * 100)
  const startDate = new Date(tournament.start_time)
  const isFull = tournament.current_players >= tournament.max_players

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="card p-6 mb-6"
        style={{ background: 'linear-gradient(135deg, #1a0a2e, #0e1a2e)', borderColor: '#3b1a6a' }}>
        <div className="flex flex-wrap justify-between items-start gap-4">
          <div>
            <span className={`badge-${tournament.status === 'live' ? 'live' : 'upcoming'} mb-3 inline-block`}>
              {tournament.status === 'live' ? '🔴 LIVE' : '⏳ Upcoming'}
            </span>
            <h1 className="text-3xl font-black text-white mb-1">{tournament.title}</h1>
            <p className="text-purple-400">{tournament.game} • {tournament.mode} • {tournament.map || 'All Maps'}</p>
          </div>
          <div className="text-right">
            <div className="text-xs text-gray-500 mb-1">Prize Pool</div>
            <div className="text-4xl font-black text-[#f59e0b]">₹{tournament.prize_pool?.toLocaleString()}</div>
          </div>
        </div>
      </div>

      <div className="grid md:grid-cols-3 gap-6">
        {/* Left — details */}
        <div className="md:col-span-2 space-y-4">
          {/* Stats */}
          <div className="card p-5">
            <h2 className="font-bold text-white mb-4">Tournament Info</h2>
            <div className="grid grid-cols-2 gap-4">
              {[
                { label: 'Entry Fee', value: tournament.entry_fee === 0 ? '🆓 Free' : `₹${tournament.entry_fee}`, color: 'text-purple-400' },
                { label: 'Mode', value: tournament.mode, color: 'text-white' },
                { label: 'Max Players', value: tournament.max_players, color: 'text-white' },
                { label: 'Starts', value: startDate.toLocaleString('en-IN'), color: 'text-cyan-400' },
              ].map((s, i) => (
                <div key={i} className="bg-[#1a1a35] rounded-xl p-3">
                  <div className="text-xs text-gray-500 mb-1">{s.label}</div>
                  <div className={`font-bold ${s.color}`}>{s.value}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Prize distribution */}
          <div className="card p-5">
            <h2 className="font-bold text-white mb-4">🏆 Prize Distribution</h2>
            <div className="space-y-2">
              {(tournament.prize_distribution || [
                { rank: 1, prize: tournament.prize_pool * 0.5 },
                { rank: 2, prize: tournament.prize_pool * 0.3 },
                { rank: 3, prize: tournament.prize_pool * 0.2 },
              ]).map((p: any) => (
                <div key={p.rank} className="flex justify-between items-center bg-[#1a1a35] rounded-lg p-3">
                  <span className="text-gray-400">
                    {p.rank === 1 ? '🥇' : p.rank === 2 ? '🥈' : p.rank === 3 ? '🥉' : `#${p.rank}`} Place
                  </span>
                  <span className="font-bold text-[#f59e0b]">₹{p.prize?.toLocaleString()}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Registered players */}
          <div className="card p-5">
            <h2 className="font-bold text-white mb-4">
              <Users size={16} className="inline mr-2" />
              Registered Players ({players.length})
            </h2>
            {players.length === 0 ? (
              <p className="text-gray-500 text-sm">No players registered yet. Be the first!</p>
            ) : (
              <div className="space-y-2 max-h-60 overflow-y-auto">
                {players.map((p: any, i: number) => (
                  <div key={i} className="flex justify-between items-center bg-[#1a1a35] rounded-lg px-3 py-2 text-sm">
                    <span className="text-gray-400">#{p.slot}</span>
                    <span className="text-white font-semibold">{p.in_game_name}</span>
                    <span className="text-gray-500">{p.team_name || '—'}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Rules */}
          {tournament.rules && (
            <div className="card p-5">
              <h2 className="font-bold text-white mb-3">📋 Rules</h2>
              <p className="text-gray-400 text-sm whitespace-pre-line">{tournament.rules}</p>
            </div>
          )}
        </div>

        {/* Right — register */}
        <div className="space-y-4">
          {/* Slots */}
          <div className="card p-5">
            <h2 className="font-bold text-white mb-3">Slot Availability</h2>
            <div className="flex justify-between text-sm mb-2">
              <span className="text-gray-400">{tournament.current_players} / {tournament.max_players} filled</span>
              <span className="text-purple-400">{tournament.max_players - tournament.current_players} left</span>
            </div>
            <div className="bg-[#1e1e3a] rounded-full h-3 mb-4">
              <div className="h-full rounded-full transition-all"
                style={{ width: `${fillPct}%`, background: 'linear-gradient(90deg,#7c3aed,#06b6d4)' }} />
            </div>

            {tournament.status === 'upcoming' && !isFull ? (
              <button onClick={() => setShowRegModal(true)} className="btn-primary w-full py-3 text-base">
                {tournament.entry_fee > 0 ? `Pay ₹${tournament.entry_fee} & Register` : 'Register Free'}
                <ChevronRight size={16} className="inline ml-1" />
              </button>
            ) : isFull ? (
              <button disabled className="w-full py-3 rounded-xl bg-gray-800 text-gray-500 font-bold cursor-not-allowed">
                Tournament Full
              </button>
            ) : (
              <button disabled className="w-full py-3 rounded-xl bg-gray-800 text-gray-500 font-bold cursor-not-allowed">
                Registration Closed
              </button>
            )}

            <div className="flex items-center gap-2 mt-3 text-xs text-gray-500">
              <Shield size={12} /> Secure payment via Razorpay
            </div>
          </div>
        </div>
      </div>

      {/* Registration Modal */}
      {showRegModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center px-4"
          style={{ background: 'rgba(0,0,0,0.8)' }}>
          <div className="card p-6 w-full max-w-md relative">
            <button onClick={() => setShowRegModal(false)}
              className="absolute top-4 right-4 text-gray-500 hover:text-white">
              <X size={20} />
            </button>

            <h2 className="text-xl font-bold text-white mb-1">Register for Tournament</h2>
            <p className="text-gray-500 text-sm mb-5">{tournament.title}</p>

            <div className="space-y-4">
              <div>
                <label className="text-xs text-gray-400 mb-1.5 block">In-Game Name *</label>
                <input
                  placeholder="Your username in the game"
                  value={regForm.in_game_name}
                  onChange={e => setRegForm(p => ({ ...p, in_game_name: e.target.value }))}
                />
              </div>

              <div>
                <label className="text-xs text-gray-400 mb-1.5 block">Team Name (optional)</label>
                <input
                  placeholder="e.g. Team Alpha"
                  value={regForm.team_name}
                  onChange={e => setRegForm(p => ({ ...p, team_name: e.target.value }))}
                />
              </div>

              {/* Summary */}
              <div className="bg-[#1a1a35] rounded-xl p-4 space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-400">Tournament</span>
                  <span className="text-white font-semibold">{tournament.title}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-400">Entry Fee</span>
                  <span className="text-purple-400 font-bold">
                    {tournament.entry_fee === 0 ? 'FREE' : `₹${tournament.entry_fee}`}
                  </span>
                </div>
                <div className="border-t border-[#2a2a4a] pt-2 flex justify-between text-sm">
                  <span className="text-gray-400">You pay</span>
                  <span className="text-[#f59e0b] font-black text-base">
                    {tournament.entry_fee === 0 ? '₹0' : `₹${tournament.entry_fee}`}
                  </span>
                </div>
              </div>

              <button onClick={handleRegister} disabled={paying} className="btn-primary w-full py-3 text-base">
                {paying ? '⏳ Processing...' : tournament.entry_fee > 0 ? `Pay ₹${tournament.entry_fee} & Confirm Slot` : 'Confirm Registration'}
              </button>

              <p className="text-xs text-gray-500 text-center flex items-center justify-center gap-1">
                <Shield size={11} /> Your slot is confirmed only after successful payment
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

const DEMO_TOURNAMENT = {
  match_id: '1', title: 'Friday Night Showdown', game: 'Free Fire MAX', mode: 'Squad',
  map: 'Bermuda', entry_fee: 50, prize_pool: 5000, max_players: 48, current_players: 32,
  start_time: new Date(Date.now() + 86400000).toISOString(), status: 'upcoming',
  rules: '• No teaming with enemies\n• Screenshot proof required\n• Results within 30 min of match end',
  prize_distribution: [{ rank: 1, prize: 2500 }, { rank: 2, prize: 1500 }, { rank: 3, prize: 1000 }],
}
const DEMO_PLAYERS = [
  { slot: 1, in_game_name: 'SnipeKing', team_name: 'Team Alpha' },
  { slot: 2, in_game_name: 'BlazeShot', team_name: 'Team Beta' },
  { slot: 3, in_game_name: 'ProGamer99', team_name: null },
]
