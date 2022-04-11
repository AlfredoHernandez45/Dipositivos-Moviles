/*Crear un programa en kotlin, que cree objetos de tipo fraccionado (3/4, 2/5), 
 * que sea capaz de suma, restar, multiplicar y dividir fracciones. 
 * Debera devolver una fracción como resultado.
 * 
 * extencion .kt
*/

class Funcion{

    val frac1: String
    val frac2: String
    val valores1: ArrayList<String> = ArrayList()
    val valores2: ArrayList<String> = ArrayList()
    
    constructor(frac1: String, frac2: String){
        this.frac1 = frac1
        this.frac2 = frac2
    }
    
	fun extraerFrac1(){
       for(valor in this.frac1){
        	if(valor !in "/"){
                valores1.add(valor.toString())
            }
        }
    }
    
    fun extraerFrac2(){
        for(valor in this.frac2){
        	if(valor !in "/"){
                valores2.add(valor.toString())
            }
        }
    }
    
    fun suma(){
        val primer1 = this.valores1.get(0).toInt()
        val segunda1 = this.valores1.get(1).toInt()
        val primer2 = this.valores2.get(0).toInt()
        val segunda2 = this.valores2.get(1).toInt()
        
        val multiplicacion1 = primer1*segunda2
        val multiplicacion2 = segunda1*primer2
        val denominador = segunda1*segunda2
        val suma = multiplicacion1+multiplicacion2
        
        println("La Suma es: $suma / $denominador")
    }
    
    fun resta(){
        val primer1 = this.valores1.get(0).toInt()
        val segunda1 = this.valores1.get(1).toInt()
        val primer2 = this.valores2.get(0).toInt()
        val segunda2 = this.valores2.get(1).toInt()
        
        val multiplicacion1 = primer1*segunda2
        val multiplicacion2 = segunda1*primer2
        val denominador = segunda1*segunda2
        val resta = multiplicacion1-multiplicacion2
        
        println("La Resta es: $resta / $denominador")
    }
    
    fun multiplicacion(){
        val numerador1 = this.valores1.get(0).toInt()
        val denominador1 = this.valores1.get(1).toInt()
        val numerador2 = this.valores2.get(0).toInt()
        val denominador2 = this.valores2.get(1).toInt()
        
        val numerador = numerador1*numerador2
        val denominador = denominador1*denominador2
        
        println("La Multiplicacion es: $numerador / $denominador")
    }
    
    fun division(){
        val numerador1 = this.valores1.get(0).toInt()
        val denominador1 = this.valores1.get(1).toInt()
        val numerador2 = this.valores2.get(0).toInt()
        val denominador2 = this.valores2.get(1).toInt()
        
        val numerador = numerador1*denominador2
        val denominador = denominador1*numerador2
        
        println("La Division es: $numerador / $denominador")
    }
    
}

fun main(args: Array<String>) {
   	val frac1:String = "3/4"
    val frac2:String = "2/5"
	val fraccion = Funcion(frac1, frac2)
    
    fraccion.extraerFrac1()
    fraccion.extraerFrac2()
    
    fraccion.suma()
    fraccion.resta()
    fraccion.multiplicacion()
    fraccion.division()
}
