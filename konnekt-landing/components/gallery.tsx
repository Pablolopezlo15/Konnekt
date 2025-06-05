"use client"

import { motion } from "framer-motion"
import { Card, CardContent } from "@/components/ui/card"

const galleryImages = [
  {
    src: "/1000112177.jpg",
    title: "Diseño Moderno",
    description: "Interfaz elegante y contemporánea"
  },
  {
    src: "/unnamed (2).jpg",
    title: "Experiencia Visual",
    description: "Contenido rich media optimizado"
  }
]

export default function Gallery() {
  return (
    <section id="gallery" className="py-24 px-4 bg-gradient-to-br from-gray-50 to-orange-50">
      <div className="container mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          viewport={{ once: true }}
          className="text-center mb-16"
        >
          <h2 className="text-4xl md:text-5xl font-bold mb-6 bg-gradient-to-r from-orange-600 to-orange-500 bg-clip-text text-transparent">
            Galería Visual
          </h2>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto">
            Descubre la estética y el diseño cuidadosamente elaborado de Konnekt
          </p>
        </motion.div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-4xl mx-auto">
          {galleryImages.map((image, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: index * 0.2 }}
              viewport={{ once: true }}
              whileHover={{ y: -10, scale: 1.02 }}
              className="h-full"
            >
              <Card className="overflow-hidden border-orange-200 hover:border-orange-300 transition-all duration-300 hover:shadow-xl h-full">
                <CardContent className="p-0 h-full">
                  <div className="relative h-full flex flex-col">
                    <img
                      src={image.src}
                      alt={image.title}
                      className="w-full h-80 object-cover"
                    />
                    <div className="p-6 bg-white flex-grow">
                      <h3 className="text-xl font-semibold mb-2 text-gray-800">{image.title}</h3>
                      <p className="text-gray-600">{image.description}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}
