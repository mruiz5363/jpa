package test;

import modelo.Persona;
import persistence.Persistence;

/**
 * @author INDRA <br>
 *         Claudia Patricia Fernandez Quitian<br>
 *         cpfernandezq@indracompany.com<br>
 * 
 * @date 23/10/2019
 * @version 1.0
 */
public class Prueba2 {

	public static void main(String[] args) {
		
		Persistence persistence = new Persistence();
		try {
			persistence.persist(new Persona("Claudia", "54321", 29));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
