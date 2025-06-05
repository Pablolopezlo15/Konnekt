import type React from "react"
import type { Metadata } from "next"
import { Inter } from "next/font/google"
import "./globals.css"

const inter = Inter({ subsets: ["latin"] })

export const metadata: Metadata = {
  title: "Konnekt - Red Social con IA",
  description:
    "La red social del futuro con inteligencia artificial integrada. Proyecto integrado DAM por Pablo López Lozano.",
  keywords: "red social, android, kotlin, jetpack compose, inteligencia artificial, DAM, proyecto integrado",
  authors: [{ name: "Pablo López Lozano" }],
  icons: {
    icon: [
      { url: "/favicon.ico" },
      { url: "logo.png", type: "image/png" }
    ],
    shortcut: "/favicon.ico",
    apple: "logo.png",
  },
  openGraph: {
    title: "Konnekt - Red Social con IA",
    description: "La red social del futuro con inteligencia artificial integrada",
    type: "website",
    images: ["logo.png"],
  },
    generator: 'v0.dev'
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="es">
      <body className={inter.className}>{children}</body>
    </html>
  )
}
