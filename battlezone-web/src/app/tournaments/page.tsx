'use client'
import { useEffect, useState } from 'react'
import { useSearchParams } from 'next/navigation'
import api from '@/lib/api'
import TournamentCard from '@/components/TournamentCard'
import { Search, Filter } from 'lucide-react'
import { Suspense } from 'react'

const GAMES = ['All', 'Free Fire MAX', 'PUBG Mobile', 'Call of Duty Mobile', 'BGMI']
const STATUSES = ['upcoming', 'live', 'completed']

function TournamentsContent() {
  const searchParams = useSearchParams()
  const [tournaments, setTournaments] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [status, setStatus] = useState('upcoming')
  const [game, setGame] = useState(searchParams.get('game') || 'All')
  const [search, setSearch] = useState('')

  useEffect(() => {
    setLoading(true)
    const params = new URLSearchParams({ status, limit: '30' })
    if (game !== 'All') params.set('game', game)
    api.get(`/api/matches/?${params}`)
      .then(r => setTournaments(r.data.matches || []))
      .catch(() => setTournaments(DEMO_TOURNAMENTS))
      .finally(() => setLoading(false))
  }, [status, game])

  const filtered = tournaments.filter(t =>
    t.title.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-black text-white mb-6">🎮 Tournaments</h1>

      {/* Filters */}
      <div className="flex flex-wrap gap-3 mb-6">
        {/* Search */}
        <div className="flex items-center gap-2 bg-[#12122a] border border-[#2a1a4a] rounded-xl px-3 py-2 flex-1 min-w-[200px]">
          <Search size={16} className="text-gray-500" />
          <input
            className="bg-transparent border-none outline-none text-sm text-gray-300 w-full p-0"
            placeholder="Search tournaments..."
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>

        {/* Status tabs */}
        <div className="flex gap-2">
          {STATUSES.map(s => (
            <button key={s} onClick={() => setStatus(s)}
              className="px-4 py-2 rounded-xl text-sm font-semibold transition-all capitalize"
              style={{
                background: status === s ? 'linear-gradient(135deg,#7c3aed,#5b21b6)' : '#12122a',
                color: status === s ? 'white' : '#888',
                border: `1px solid ${status === s ? '#7c3aed' : '#2a1a4a'}`,
              }}>
              {s === 'live' ? '🔴 ' : ''}{s}
            </button>
          ))}
        </div>
      </div>

      {/* Game filter */}
      <div className="flex gap-2 flex-wrap mb-6">
        {GAMES.map(g => (
          <button key={g} onClick={() => setGame(g)}
            className="px-3 py-1.5 rounded-full text-xs font-semibold transition-all"
            style={{
              background: game === g ? '#7c3aed22' : 'transparent',
              color: game === g ? '#a78bfa' : '#555',
              border: `1px solid ${game === g ? '#7c3aed' : '#2a2a4a'}`,
            }}>
            {g}
          </button>
        ))}
      </div>

      {/* Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1,2,3,4,5,6].map(i => <div key={i} className="card p-4 animate-pulse h-48" />)}
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-20 text-gray-500">
          <div className="text-4xl mb-3">🎮</div>
          <div>No tournaments found</div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((t: any) => <TournamentCard key={t.match_id} t={t} />)}
        </div>
      )}
    </div>
  )
}

export default function TournamentsPage() {
  return (
    <Suspense fallback={<div className="max-w-7xl mx-auto px-4 py-8 text-gray-400">Loading...</div>}>
      <TournamentsContent />
    </Suspense>
  )
}

const DEMO_TOURNAMENTS = [
  { match_id: '1', title: 'Friday Night Showdown', game: 'Free Fire MAX', mode: 'Squad', entry_fee: 50, prize_pool: 5000, max_players: 48, current_players: 32, start_time: new Date(Date.now() + 86400000).toISOString(), status: 'upcoming' },
  { match_id: '2', title: 'PUBG Pro League', game: 'PUBG Mobile', mode: 'Squad', entry_fee: 100, prize_pool: 10000, max_players: 64, current_players: 60, start_time: new Date(Date.now() + 3600000).toISOString(), status: 'upcoming' },
  { match_id: '3', title: 'COD Weekend Cup', game: 'Call of Duty Mobile', mode: 'Squad', entry_fee: 0, prize_pool: 2000, max_players: 32, current_players: 18, start_time: new Date(Date.now() + 172800000).toISOString(), status: 'upcoming' },
  { match_id: '4', title: 'BGMI Champions', game: 'BGMI', mode: 'Squad', entry_fee: 200, prize_pool: 20000, max_players: 100, current_players: 74, start_time: new Date(Date.now() + 259200000).toISOString(), status: 'upcoming' },
]
