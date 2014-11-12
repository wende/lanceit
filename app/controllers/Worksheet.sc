case class Test (a: String, b: String)

val a = Test("Yo", "Mama")
val b = Test("Yo", "Mam")

print(a == b)