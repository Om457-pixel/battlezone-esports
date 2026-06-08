import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface User {
  uid: string
  username: string
  phone?: string
  wallet_balance: number
  bonus_balance: number
  level: number
  rank_tier: string
  avatar?: string
  stats: {
    matches_played: number
    matches_won: number
    total_kills: number
    win_rate: number
    rank_points: number
  }
}

interface AuthStore {
  token: string | null
  user: User | null
  setToken: (token: string) => void
  setUser: (user: User) => void
  logout: () => void
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      setToken: (token) => {
        localStorage.setItem('bz_token', token)
        set({ token })
      },
      setUser: (user) => set({ user }),
      logout: () => {
        localStorage.removeItem('bz_token')
        set({ token: null, user: null })
      },
    }),
    { name: 'battlezone-auth' }
  )
)
