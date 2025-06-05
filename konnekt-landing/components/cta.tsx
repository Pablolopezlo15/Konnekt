"use client"

import { motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import { Download } from "lucide-react"

export default function CTA() {
  return (
    <section className="py-24 px-4 relative overflow-hidden">
      <div className="absolute inset-0 bg-gradient-to-br from-orange-600 via-orange-500 to-orange-400 z-0"></div>

      {/* Decorative elements */}
      <div className="absolute top-0 left-0 w-full h-full overflow-hidden z-0">
        <div className="absolute top-10 left-10 w-64 h-64 bg-white/10 rounded-full blur-3xl"></div>
        <div className="absolute bottom-10 right-10 w-80 h-80 bg-orange-300/20 rounded-full blur-3xl"></div>
      </div>

      <div className="container mx-auto text-center relative z-10">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          viewport={{ once: true }}
          className="max-w-3xl mx-auto"
        >
          <h2 className="text-4xl md:text-5xl font-bold text-white mb-6">¿Listo para Conectar?</h2>
          <p className="text-xl text-orange-100 mb-10 max-w-2xl mx-auto">
            Descarga Konnekt y experimenta el futuro de las redes sociales con IA integrada
          </p>

          <div className="flex justify-center">
            <a
              href="https://drive.google.com/file/d/1c7hhzFf_HxUFwnPhWVu_KV4zJ2jYsNsl/view?usp=sharing"
              target="_blank"
              rel="noopener noreferrer"
            >
              <Button
                size="lg"
                variant="secondary"
                className="bg-white text-orange-600 hover:bg-orange-50 text-lg px-12 py-6"
              >
                <Download className="w-5 h-5 mr-2" />
                Descargar APK
              </Button>
            </a>
          </div>

        </motion.div>
      </div>
    </section>
  )
}
