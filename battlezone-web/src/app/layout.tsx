import type { Metadata } from 'next'
import './globals.css'
import { Toaster } from 'react-hot-toast'
import Navbar from '@/components/Navbar'

export const metadata: Metadata = {
  title: 'BattleZone — Esports Tournament Platform',
  description: 'Host, join and watch esports tournaments. Free Fire MAX, PUBG Mobile, COD Mobile, BGMI.',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <Navbar />
        <main className="min-h-screen">{children}</main>
        <Toaster
          position="top-right"
          toastOptions={{
            style: { background: '#12122a', color: '#e0e0ff', border: '1px solid #2a1a4a' },
          }}
        />
      </body>
    </html>
  )
}
