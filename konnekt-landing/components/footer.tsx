"use client"

import { motion } from "framer-motion"
import Link from "next/link"
import { Github, Linkedin, Mail } from "lucide-react"

export default function Footer() {
  return (
    <footer className="py-16 px-4 bg-gray-900 text-white">
      <div className="container mx-auto">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-12">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            viewport={{ once: true }}
            className="md:col-span-2"
          >
            <div className="flex items-center space-x-3 mb-6">
              <img 
                src="/logo.png" 
                alt="Konnekt Logo" 
                className="w-10 h-10 rounded-xl"
              />
              <span className="text-2xl font-bold bg-gradient-to-r from-orange-400 to-orange-500 bg-clip-text text-transparent">
                Konnekt
              </span>
            </div>

            <p className="text-gray-400 mb-8 max-w-md leading-relaxed">
              Proyecto Integrado de Desarrollo de Aplicaciones Multiplataforma (DAM). Una red social nativa para Android
              con inteligencia artificial integrada.
            </p>

            <div className="space-y-6">
              <h4 className="text-orange-400 font-semibold text-lg">Conecta conmigo</h4>
              
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
                <a
                  href="mailto:plopezlozano12@gmail.com"
                  className="flex items-center gap-3 text-gray-400 hover:text-orange-400 transition-all duration-300 group p-3 rounded-lg hover:bg-gray-800/50"
                >
                  <div className="w-10 h-10 bg-gray-800 rounded-lg flex items-center justify-center group-hover:bg-orange-500/20 transition-all duration-300">
                    <Mail className="w-5 h-5" />
                  </div>
                  <div className="min-w-0">
                    <p className="font-medium">Email</p>
                    <p className="text-sm opacity-80 truncate">plopezlozano12@gmail.com</p>
                  </div>
                </a>

                <a
                  href="https://github.com/pablolopezlo15"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-3 text-gray-400 hover:text-orange-400 transition-all duration-300 group p-3 rounded-lg hover:bg-gray-800/50"
                >
                  <div className="w-10 h-10 bg-gray-800 rounded-lg flex items-center justify-center group-hover:bg-orange-500/20 transition-all duration-300">
                    <Github className="w-5 h-5" />
                  </div>
                  <div className="min-w-0">
                    <p className="font-medium">GitHub</p>
                    <p className="text-sm opacity-80">@pablolopezlo15</p>
                  </div>
                </a>

                <a
                  href="https://linkedin.com/in/pablolopezlozano"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-3 text-gray-400 hover:text-orange-400 transition-all duration-300 group p-3 rounded-lg hover:bg-gray-800/50"
                >
                  <div className="w-10 h-10 bg-gray-800 rounded-lg flex items-center justify-center group-hover:bg-orange-500/20 transition-all duration-300">
                    <Linkedin className="w-5 h-5" />
                  </div>
                  <div className="min-w-0">
                    <p className="font-medium">LinkedIn</p>
                    <p className="text-sm opacity-80">Pablo López Lozano</p>
                  </div>
                </a>
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 30 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.2 }}
            viewport={{ once: true }}
            className="space-y-8"
          >
            <div>
              <h3 className="text-lg font-semibold mb-4 text-orange-400">Navegación</h3>
              <ul className="space-y-3">
                <li>
                  <Link href="#" className="text-gray-400 hover:text-orange-400 transition-colors flex items-center gap-2 group">
                    <span className="w-1 h-1 bg-orange-400 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"></span>
                    Inicio
                  </Link>
                </li>
                <li>
                  <Link href="#features" className="text-gray-400 hover:text-orange-400 transition-colors flex items-center gap-2 group">
                    <span className="w-1 h-1 bg-orange-400 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"></span>
                    Características
                  </Link>
                </li>
                <li>
                  <Link href="#experiencia" className="text-gray-400 hover:text-orange-400 transition-colors flex items-center gap-2 group">
                    <span className="w-1 h-1 bg-orange-400 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"></span>
                    Experiencia
                  </Link>
                </li>
                <li>
                  <Link href="#tech" className="text-gray-400 hover:text-orange-400 transition-colors flex items-center gap-2 group">
                    <span className="w-1 h-1 bg-orange-400 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"></span>
                    Tecnologías
                  </Link>
                </li>
              </ul>
            </div>

            <div>
              <h3 className="text-lg font-semibold mb-4 text-orange-400">Descargar</h3>
              <a
                href="https://drive.google.com/file/d/1nCNsHlGMNvKSZyEqJJqOA2xT7UuIMJjL/view?usp=sharing"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 bg-gradient-to-r from-orange-500 to-orange-600 text-white px-6 py-3 rounded-lg font-semibold hover:from-orange-600 hover:to-orange-700 transition-all duration-300 shadow-lg hover:shadow-orange-500/25"
              >
                📱 Descargar APK
              </a>
            </div>
          </motion.div>

          
        </div>

        <div className="border-t border-gray-800 mt-12 pt-8 text-center">
          <p className="text-gray-500 text-sm">
            © 2025 Pablo López Lozano. Desarrollado para el proyecto integrado de DAM.
          </p>
          <p className="text-gray-600 text-xs mt-2">I.E.S. Hermenegildo Lanz - Granada</p>
        </div>
      </div>
    </footer>
  )
}
