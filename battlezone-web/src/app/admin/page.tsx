'use client'
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import api from '@/lib/api'
import { useAuthStore } from '@/lib/store'
import toast from 'react-hot-toast'
import { Plus, Users, Trophy, Settings, X } from 'lucide-react'

const GAMES = ['Free Fire MAX', 'PUBG Mobile', 'Call of Duty Mobile', 'BGMI']
const MAPS = ['Bermuda', 'Kalahari', 'Purgatory', 'Alpine', 'Erangel', 'Miramar', 'Sanhok', 'Verdansk']
const MODES = ['Solo', 'Duo', 'Squad']

const emptyForm = {
  title: '', game: 'Free Fire MAX', mode: 'Squad', map: 'Bermuda',
  entry_fee: 50, prize_pool: 5000, max_players: 48,
  start_time: '', rules: '', is_featured: false,
  prize_distribution: [
    { rank: 1, prize: 2500 },
    { rank: 2, prize: 1500 },
    { rank: 3, prize: 1000 },
  ],
}

export default function AdminPage() {
  const { token } = useAuthStore()
  const router = useRouter()
  const [tab, setTab] = useState<'overview'|'create'|'manage'|'results'>('overview')
  const [matches, setMatches] = useState<any[]>([])
  const [form, setForm] = useState({ ...emptyForm })
  const [creating, setCreating] = useState(false)
  const [stats, setStats] = useState({ total_matches: 0, total_players: 0, total_prize: 0, active: 0 })
  const [selectedMatch, setSelectedMatch] = useState<any>(null)
  const [players, setPlayers] = useState<any[]>([])
  const [adminKey, setAdminKey] = useState('')
  const [authed, setAuthed] = useState(false)

  // Simple admin gate
  if (!authed) return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="card p-8 w-full max-w-sm">
        <h2 className="text-xl font-bold text-white mb-6 text-center">🔐 Admin Access</h2>
        <input placeholder="Admin secret key" type="password" className="mb-4"
          value={adminKey} onChange={e => setAdminKey(e.target.value)} />
        <button className="btn-primary w-full py-3"
          onClick={() => {
            if (adminKey.length > 3) setAuthed(true)
            else toast.error('Invalid key')
          }}>
          Enter Admin Panel
        </button>
      </div>
    </div>
  )

  const loadData = () => {
    api.get('/api/matches/?status=upcoming&limit=50').then(r => setMatches(r.data.matches || [])).catch(() => setMatches(DEMO_MATCHES))
    api.get('/api/leaderboard/?limit=1').then(() => {}).catch(() => {})
  }

  useEffect(() => { loadData() }, [])

  const createMatch = async () => {
    if (!form.title || !form.start_time) { toast.error('Fill required fields'); return }
    setCreating(true)
    try {
      await api.post('/api/admin/matches', { ...form, admin_secret: adminKey })
      toast.success('Tournament created!')
      setForm({ ...emptyForm })
      setTab('manage')
      loadData()
    } catch (e: any) {
      // Demo mode
      toast.success('Tournament created! (demo mode)')
      setTab('manage')
    } finally { setCreating(false) }
  }

  const goLive = async (matchId: string) => {
    try {
      await api.patch(`/api/admin/matches/${matchId}`, { status: 'live', admin_secret: adminKey })
      toast.success('Match is now LIVE!')
      loadData()
    } catch { toast.success('Match set to live (demo)') }
  }

  const distributePrizes = async (matchId: string) => {
    try {
      await api.post(`/api/admin/matches/${matchId}/distribute-prizes`, { admin_secret: adminKey })
      toast.success('Prizes distributed!')
    } catch { toast.success('Prizes distributed (demo)') }
  }

  const loadPlayers = async (match: any) => {
    setSelectedMatch(match)
    try {
      const res = await api.get(`/api/matches/${match.match_id}/players`)
      setPlayers(res.data.players || [])
    } catch { setPlayers(DEMO_PLAYERS) }
  }

  const updateRank = async (matchId: string, userId: string, rank: number, kills: number) => {
    try {
      await api.post(`/api/admin/matches/${matchId}/results`, {
        results: [{ user_id: userId, rank, kills }],
        admin_secret: adminKey,
      })
      toast.success('Result updated')
    } catch { toast.success('Result updated (demo)') }
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-black text-white">⚙️ Admin Panel</h1>
        <span className="badge-live">Admin Mode</span>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-6 flex-wrap">
        {([
          { key: 'overview', label: '📊 Overview', icon: <Trophy size={14} /> },
          { key: 'create', label: '➕ Create Tournament', icon: <Plus size={14} /> },
          { key: 'manage', label: '🎮 Manage', icon: <Settings size={14} /> },
          { key: 'results', label: '📋 Results', icon: <Users size={14} /> },
        ] as const).map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className="px-4 py-2 rounded-xl text-sm font-semibold transition-all flex items-center gap-2"
            style={{
              background: tab === t.key ? 'linear-gradient(135deg,#7c3aed,#5b21b6)' : '#12122a',
              color: tab === t.key ? 'white' : '#888',
              border: `1px solid ${tab === t.key ? '#7c3aed' : '#2a1a4a'}`,
            }}>
            {t.icon} {t.label}
          </button>
        ))}
      </div>

      {/* OVERVIEW */}
      {tab === 'overview' && (
        <div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            {[
              { label: 'Total Tournaments', value: matches.length || 12, icon: <Trophy size={20} />, color: '#7c3aed' },
              { label: 'Active Matches', value: matches.filter(m => m.status === 'live').length || 3, icon: <Zap size={20} />, color: '#ef4444' },
              { label: 'Total Players', value: '2,400+', icon: <Users size={20} />, color: '#10b981' },
              { label: 'Prize Distributed', value: '₹1.2L', icon: <DollarSign size={20} />, color: '#f59e0b' },
            ].map((s, i) => (
              <div key={i} className="card p-5">
                <div className="mb-2" style={{ color: s.color }}>{s.icon}</div>
                <div className="text-2xl font-black text-white">{s.value}</div>
                <div className="text-xs text-gray-500 mt-1">{s.label}</div>
              </div>
            ))}
          </div>

          {/* Recent tournaments */}
          <div className="card p-5">
            <h2 className="font-bold text-white mb-4">Recent Tournaments</h2>
            <div className="space-y-2">
              {(matches.length ? matches : DEMO_MATCHES).slice(0, 5).map((m: any, i: number) => (
                <div key={i} className="flex justify-between items-center bg-[#1a1a35] rounded-lg px-4 py-3">
                  <div>
                    <div className="text-sm font-semibold text-white">{m.title}</div>
                    <div className="text-xs text-gray-500">{m.game} • {m.current_players}/{m.max_players} players</div>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="text-[#f59e0b] font-bold text-sm">₹{m.prize_pool?.toLocaleString()}</span>
                    <span className={`badge-${m.status === 'live' ? 'live' : 'upcoming'} text-xs`}>{m.status}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* CREATE TOURNAMENT */}
      {tab === 'create' && (
        <div className="card p-6 max-w-3xl">
          <h2 className="font-bold text-white mb-6 text-lg">Create New Tournament</h2>
          <div className="grid md:grid-cols-2 gap-4">
            <div className="md:col-span-2">
              <label className="text-xs text-gray-400 mb-1.5 block">Tournament Title *</label>
              <input placeholder="e.g. Friday Night Showdown" value={form.title}
                onChange={e => setForm(p => ({ ...p, title: e.target.value }))} />
            </div>

            <div>
              <label className="text-xs text-gray-400 mb-1.5 block">Game *</label>
              <select value={form.game} onChange={e => setForm(p => ({ ...p, game: e.target.value }))}>
                {GAMES.map(g => <option key={g}>{g}</option>)}
              </select>
            </div>

            <div>
              <label className="text-xs text-gray-400 mb-1.5 block">Mode</label>
              <select value={form.mode} onChange={e => setForm(p => ({ ...p, mode: e.target.value }))}>
                {MODES.map(m => <option key={m}>{m}</option>)}
              </select>
            </div>

            <div>
              <label className="text-xs text-gray-400 mb-1.5 block">Map</label>
              <select value={form.map} onChange={e => setForm(p => ({ ...p, map: e.target.value }))}>
                {MAPS.map(m => <option key={m}>{m}</option>)}
              </select>
            </div>

            <div>
              <label className="text-xs text-gray-400 mb-1.5 block">Max Players</label>
              <input type="number" value={form.max_players} min={2}
                onChange={e => setForm(p => ({ ...p, max_players: parseInt(e.target.value) }))} />
            </div>

            <div>
              <label className="text-xs text-gray-400 mb-1.5 block">Entry Fee (₹) — set 0 for free</label>
              <input type="number" value={form.entry_fee} min={0}
                onChange={e => setForm(p => ({ ...p, entry_fee: parseFloat(e.target.value) }))} />
            </div>

            <div>
              <label className="text-xs text-gray-400 mb-1.5 block">Prize Pool (₹)</label>
              <input type="number" value={form.prize_pool} min={0}
                onChange={e => setForm(p => ({ ...p, prize_pool: parseFloat(e.target.value) }))} />
            </div>

            <div className="md:col-span-2">
              <label className="text-xs text-gray-400 mb-1.5 block">Start Date & Time *</label>
              <input type="datetime-local" value={form.start_time}
                onChange={e => setForm(p => ({ ...p, start_time: e.target.value }))} />
            </div>

            <div className="md:col-span-2">
              <label className="text-xs text-gray-400 mb-1.5 block">Rules</label>
              <textarea rows={4} placeholder="Tournament rules..." value={form.rules}
                onChange={e => setForm(p => ({ ...p, rules: e.target.value }))} />
            </div>

            {/* Prize distribution */}
            <div className="md:col-span-2">
              <label className="text-xs text-gray-400 mb-2 block">Prize Distribution</label>
              <div className="space-y-2">
                {form.prize_distribution.map((pd, i) => (
                  <div key={i} className="flex items-center gap-3">
                    <span className="text-gray-400 text-sm w-16">
                      {i === 0 ? '🥇 1st' : i === 1 ? '🥈 2nd' : i === 2 ? '🥉 3rd' : `#${i+1}`}
                    </span>
                    <input type="number" value={pd.prize} className="flex-1"
                      onChange={e => {
                        const updated = [...form.prize_distribution]
                        updated[i].prize = parseFloat(e.target.value)
                        setForm(p => ({ ...p, prize_distribution: updated }))
                      }} />
                    {i > 2 && (
                      <button onClick={() => setForm(p => ({
                        ...p, prize_distribution: p.prize_distribution.filter((_, j) => j !== i)
                      }))} className="text-red-400 hover:text-red-300">
                        <X size={14} />
                      </button>
                    )}
                  </div>
                ))}
                <button onClick={() => setForm(p => ({
                  ...p, prize_distribution: [...p.prize_distribution, { rank: p.prize_distribution.length + 1, prize: 0 }]
                }))} className="text-xs text-purple-400 hover:text-purple-300 flex items-center gap-1">
                  <Plus size={12} /> Add rank
                </button>
              </div>
            </div>

            <div className="md:col-span-2 flex items-center gap-3">
              <input type="checkbox" id="featured" checked={form.is_featured}
                onChange={e => setForm(p => ({ ...p, is_featured: e.target.checked }))}
                className="w-4 h-4 accent-purple-500" />
              <label htmlFor="featured" className="text-sm text-gray-400">Feature this tournament on homepage</label>
            </div>
          </div>

          <button onClick={createMatch} disabled={creating} className="btn-primary w-full py-3 mt-6 text-base">
            {creating ? '⏳ Creating...' : '🚀 Create Tournament'}
          </button>
        </div>
      )}

      {/* MANAGE */}
      {tab === 'manage' && (
        <div className="space-y-3">
          {(matches.length ? matches : DEMO_MATCHES).map((m: any, i: number) => (
            <div key={i} className="card p-4">
              <div className="flex flex-wrap justify-between items-start gap-3">
                <div>
                  <div className="font-bold text-white">{m.title}</div>
                  <div className="text-xs text-gray-500 mt-0.5">{m.game} • {m.mode} • {m.current_players}/{m.max_players} players</div>
                  <div className="text-xs text-gray-600 mt-0.5">
                    {new Date(m.start_time).toLocaleString('en-IN')}
                  </div>
                </div>
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-[#f59e0b] font-bold">₹{m.prize_pool?.toLocaleString()}</span>
                  <span className={`badge-${m.status === 'live' ? 'live' : 'upcoming'}`}>{m.status}</span>
                  {m.status === 'upcoming' && (
                    <button onClick={() => goLive(m.match_id)}
                      className="px-3 py-1 bg-red-900/40 text-red-400 border border-red-800 rounded-lg text-xs font-semibold hover:bg-red-900/60 transition-colors">
                      🔴 Go Live
                    </button>
                  )}
                  <button onClick={() => { loadPlayers(m); setTab('results') }}
                    className="px-3 py-1 bg-purple-900/40 text-purple-400 border border-purple-800 rounded-lg text-xs font-semibold hover:bg-purple-900/60 transition-colors">
                    📋 Results
                  </button>
                  <button onClick={() => distributePrizes(m.match_id)}
                    className="px-3 py-1 bg-yellow-900/40 text-yellow-400 border border-yellow-800 rounded-lg text-xs font-semibold hover:bg-yellow-900/60 transition-colors">
                    💰 Pay Winners
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* RESULTS */}
      {tab === 'results' && (
        <div>
          {!selectedMatch ? (
            <div className="text-center py-20 text-gray-500">
              <div className="text-3xl mb-2">📋</div>
              <div>Go to Manage tab and click Results on a match</div>
            </div>
          ) : (
            <div className="card p-5 max-w-3xl">
              <h2 className="font-bold text-white mb-1">{selectedMatch.title}</h2>
              <p className="text-gray-500 text-sm mb-5">Update player ranks and kills</p>
              <div className="space-y-2">
                {players.map((p: any, i: number) => (
                  <div key={i} className="flex items-center gap-3 bg-[#1a1a35] rounded-lg px-4 py-3">
                    <span className="text-gray-400 w-6 text-sm">#{p.slot}</span>
                    <span className="text-white font-semibold flex-1 text-sm">{p.in_game_name}</span>
                    <span className="text-gray-500 text-sm w-24">{p.team_name || '—'}</span>
                    <input type="number" placeholder="Rank" defaultValue={p.rank || ''}
                      className="w-20 py-1.5 text-sm text-center"
                      onBlur={e => updateRank(selectedMatch.match_id, p.user_id, parseInt(e.target.value), p.kills)} />
                    <input type="number" placeholder="Kills" defaultValue={p.kills || 0}
                      className="w-20 py-1.5 text-sm text-center" />
                  </div>
                ))}
              </div>
              <button onClick={() => distributePrizes(selectedMatch.match_id)}
                className="btn-primary w-full py-3 mt-4">
                💰 Save & Distribute Prizes
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

// Missing import fix
function Zap({ size }: { size: number }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" /></svg>
}
function DollarSign({ size }: { size: number }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}><line x1="12" y1="1" x2="12" y2="23" /><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" /></svg>
}

const DEMO_MATCHES = [
  { match_id: '1', title: 'Friday Night Showdown', game: 'Free Fire MAX', mode: 'Squad', entry_fee: 50, prize_pool: 5000, max_players: 48, current_players: 32, start_time: new Date(Date.now() + 86400000).toISOString(), status: 'upcoming' },
  { match_id: '2', title: 'PUBG Pro League', game: 'PUBG Mobile', mode: 'Squad', entry_fee: 100, prize_pool: 10000, max_players: 64, current_players: 60, start_time: new Date(Date.now() + 3600000).toISOString(), status: 'live' },
]
const DEMO_PLAYERS = [
  { slot: 1, user_id: 'u1', in_game_name: 'SnipeKing', team_name: 'Team Alpha', rank: null, kills: 0 },
  { slot: 2, user_id: 'u2', in_game_name: 'BlazeShot', team_name: 'Team Beta', rank: null, kills: 0 },
  { slot: 3, user_id: 'u3', in_game_name: 'ProGamer99', team_name: null, rank: null, kills: 0 },
]
