"use client"

import { motion, AnimatePresence } from "framer-motion"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { X, ChevronLeft, ChevronRight } from "lucide-react"
import { useEffect } from "react"

interface Screenshot {
  title: string
  description: string
  image: string
  badge: string
  icon?: any
  color?: string
}

interface ScreenshotCarouselProps {
  screenshots: Screenshot[]
  isOpen: boolean
  currentIndex: number
  onClose: () => void
  onNext: () => void
  onPrevious: () => void
}

export default function ScreenshotCarousel({
  screenshots,
  isOpen,
  currentIndex,
  onClose,
  onNext,
  onPrevious,
}: ScreenshotCarouselProps) {
  
  // Cerrar con ESC
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
      if (e.key === 'ArrowLeft') onPrevious()
      if (e.key === 'ArrowRight') onNext()
    }

    if (isOpen) {
      document.addEventListener('keydown', handleKeyDown)
      document.body.style.overflow = 'hidden'
    }

    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = 'unset'
    }
  }, [isOpen, onClose, onNext, onPrevious])

  if (!isOpen) return null

  const currentScreenshot = screenshots[currentIndex]

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 bg-black/85 backdrop-blur-sm z-[60] flex items-center justify-center p-4"
        onClick={onClose}
      >
        {/* Contenedor principal del modal */}
        <motion.div
          initial={{ scale: 0.8, opacity: 0, y: 50 }}
          animate={{ scale: 1, opacity: 1, y: 0 }}
          exit={{ scale: 0.8, opacity: 0, y: 50 }}
          transition={{ type: "spring", damping: 25, stiffness: 300 }}
          className="relative w-full max-w-6xl h-full max-h-[90vh] bg-white rounded-2xl overflow-hidden shadow-2xl flex flex-col"
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div className="flex items-center justify-between p-6 border-b border-gray-200 bg-gradient-to-r from-orange-50 to-orange-100">
            <div className="flex items-center gap-4">
              <Badge className={`bg-gradient-to-r ${currentScreenshot.color || 'from-orange-500 to-orange-600'} text-white border-0`}>
                {currentScreenshot.badge}
              </Badge>
              <div>
                <h3 className="text-xl font-bold text-gray-800">{currentScreenshot.title}</h3>
                <p className="text-sm text-gray-600">{currentScreenshot.description}</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-sm text-gray-500 font-medium">
                {currentIndex + 1} / {screenshots.length}
              </span>
              <Button
                variant="ghost"
                size="icon"
                onClick={onClose}
                className="hover:bg-orange-100 text-gray-600 hover:text-gray-800"
              >
                <X className="w-5 h-5" />
              </Button>
            </div>
          </div>

          {/* Contenido principal */}
          <div className="flex-1 flex items-center justify-center p-6 bg-gradient-to-br from-gray-50 to-gray-100 overflow-hidden">
            <div className="relative w-full h-full flex items-center justify-center">
              {/* Imagen */}
              <motion.div
                key={currentIndex}
                initial={{ opacity: 0, x: 100 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -100 }}
                transition={{ duration: 0.3 }}
                className="relative max-w-sm mx-auto"
              >
                <img
                  src={currentScreenshot.image}
                  alt={currentScreenshot.title}
                  className="w-full h-auto max-h-[70vh] object-contain rounded-3xl shadow-2xl"
                  style={{ aspectRatio: '9/16' }}
                />
                {/* Efecto de brillo */}
                <div className="absolute inset-0 bg-gradient-to-t from-transparent via-transparent to-white/10 rounded-3xl pointer-events-none" />
              </motion.div>

              {/* Botones de navegación */}
              <Button
                variant="ghost"
                size="icon"
                onClick={onPrevious}
                className="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 bg-white/80 hover:bg-white/90 backdrop-blur-sm shadow-lg border border-gray-200/50"
                disabled={screenshots.length <= 1}
              >
                <ChevronLeft className="w-6 h-6" />
              </Button>

              <Button
                variant="ghost"
                size="icon"
                onClick={onNext}
                className="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 bg-white/80 hover:bg-white/90 backdrop-blur-sm shadow-lg border border-gray-200/50"
                disabled={screenshots.length <= 1}
              >
                <ChevronRight className="w-6 h-6" />
              </Button>
            </div>
          </div>          {/* Footer con información adicional */}
          <div className="p-6 bg-white border-t border-gray-200">
            <div className="text-center">
              <h4 className="font-semibold text-gray-800 mb-2">{currentScreenshot.title}</h4>
              <p className="text-gray-600 text-sm leading-relaxed">
                {currentScreenshot.description}
              </p>
            </div>
          </div>

          {/* Indicadores de página */}
          <div className="absolute bottom-20 left-1/2 -translate-x-1/2 flex gap-2">
            {screenshots.map((_, index) => (
              <button
                key={index}
                onClick={() => {/* Aquí podrías cambiar directamente al índice */}}
                className={`w-2 h-2 rounded-full transition-all duration-300 ${
                  index === currentIndex 
                    ? 'bg-orange-500 w-6' 
                    : 'bg-white/60 hover:bg-white/80'
                }`}
              />
            ))}
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  )
}
