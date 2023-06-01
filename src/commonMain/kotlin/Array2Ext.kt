import korlibs.datastructure.*
import korlibs.math.geom.*

operator fun <T> Array2<T>.get(p: PointInt): T = this[p.x, p.y]
operator fun <T> Array2<T>.set(p: PointInt, value: T) { this[p.x, p.y] = value }

operator fun IntArray2.get(p: PointInt): Int = this[p.x, p.y]
operator fun IntArray2.set(p: PointInt, value: Int) { this[p.x, p.y] = value }
