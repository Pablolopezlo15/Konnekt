"use client"

import { motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Download } from "lucide-react"

export default function Hero() {
  return (
    <section className="pt-32 pb-24 px-4 bg-gradient-to-br from-orange-50 via-white to-orange-100">
      <div className="container mx-auto">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="text-left"
          >
            <Badge className="mb-6 bg-orange-100 text-orange-700 hover:bg-orange-200">
              🚀 Proyecto Integrado DAM - Pablo López Lozano
            </Badge>

            <motion.div
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.2 }}
              className="flex items-center gap-4 mb-6"
            >
              <img 
                src="/logo.png" 
                alt="Konnekt Logo" 
                className="w-16 h-16 md:w-20 md:h-20 lg:w-24 lg:h-24 rounded-2xl shadow-lg"
              />
              <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold bg-gradient-to-r from-orange-600 via-orange-500 to-orange-400 bg-clip-text text-transparent">
                Konnekt
              </h1>
            </motion.div>

            <motion.p
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.4 }}
              className="text-xl text-gray-600 mb-8 leading-relaxed"
            >
              La red social del futuro con{" "}
              <span className="text-orange-500 font-semibold">inteligencia artificial</span> integrada. Conecta,
              comparte y descubre contenido de una manera completamente nueva.
            </motion.p>

            <motion.div
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.6 }}
              className="flex flex-wrap gap-4 mb-8"
            >
              <a
                href="https://drive.google.com/file/d/1c7hhzFf_HxUFwnPhWVu_KV4zJ2jYsNsl/view?usp=sharing"
                target="_blank"
                rel="noopener noreferrer"
              >
                <Button variant="outline" size="lg" className="border-orange-200 hover:bg-orange-50 text-lg px-8 py-6">
                  <Download className="w-5 h-5 mr-2" />
                  Descargar APK
                </Button>
              </a>
            </motion.div>

            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 1, delay: 0.8 }}
              className="flex items-center space-x-4 text-sm text-gray-600"
            >
              <div className="flex items-center">
                <div className="w-3 h-3 bg-green-500 rounded-full mr-2"></div>
                100% Nativo Android
              </div>
            </motion.div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 1, delay: 0.5 }}
            className="relative"
          >
            <div className="absolute inset-0 bg-gradient-to-r from-orange-400 to-orange-600 rounded-3xl blur-3xl opacity-20"></div>
            <div className="relative bg-white rounded-3xl p-6 shadow-2xl border border-orange-100">
              <div className="flex justify-between items-center mb-4">
                <div className="flex space-x-2">
                  <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                  <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
                  <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                </div>
                <div className="text-xs text-gray-400">Konnekt App</div>
              </div>
              <img
                src="/hero.png"
                alt="Konnekt App Preview"
                className="w-full max-w-sm mx-auto h-auto rounded-2xl shadow-lg"
              />
              <div className="mt-4 flex justify-center">
                <div className="w-32 h-1 bg-gray-200 rounded-full"></div>
              </div>
            </div>

            <div className="absolute -bottom-6 -right-6 bg-orange-500 text-white p-4 rounded-2xl shadow-lg transform rotate-3">
              <div className="text-sm font-semibold">¡Con IA integrada!</div>
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  )
}
