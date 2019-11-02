package persistence;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import annotations.Columna;
import annotations.Entidad;

public class Persistence {

	private static final String PROPERTIES_PATH = "/persistence.properties";

	public void persist(Object obj) throws Exception {

		String query = getSQLPersistQuery(obj);

		System.out.println(query);

		PreparedStatement pstmt = createPreparedStatement(query, obj);
		pstmt.executeUpdate();
	}

	private Connection getConnection() throws Exception {

		Properties prop = loadProperties();

		Class.forName(prop.getProperty("driver"));
		return DriverManager.getConnection(prop.getProperty("url"), prop.getProperty("user"),
				prop.getProperty("password"));
	}

	private Properties loadProperties() throws IOException {
		Properties prop = new Properties();

		InputStream input = getClass().getResourceAsStream(PROPERTIES_PATH);
		prop.load(input);

		return prop;
	}

	private PreparedStatement createPreparedStatement(String query, Object obj) throws Exception {
		Class clase = obj.getClass();
		List values = getValues(obj);
		System.out.println(values);

		Connection conn = getConnection();

		PreparedStatement pstmt = conn.prepareStatement(query);
		int i = 1;
		for (Object value : values) {
			pstmt.setObject(i, value);
			i++;
		}

		return pstmt;
	}

	private List<Object> getValues(Object obj) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		Class clase = obj.getClass();

		List list = new ArrayList<>();
		Field[] attributes = clase.getDeclaredFields();
		
		for (Field field : attributes) {
			
			Columna col = (Columna) field.getAnnotation(Columna.class);

			if (col == null)
				break;
			
			String getter = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
			Method method = clase.getMethod(getter);

			Object value = method.invoke(obj);
			System.out.println(getter + "=" + value);
			
			list.add(value);
		}

		return list;
	}

	private String getSQLPersistQuery(Object obj) {
		Class clase = obj.getClass();
		StringBuilder query = new StringBuilder();

		Entidad entidad = (Entidad) clase.getAnnotation(Entidad.class);
		
		if (entidad != null) {
			query.append("INSERT INTO ").append(entidad.schema()).append(".").append(entidad.value());
			System.out.println("Entidad=" + entidad.value() + ",esquema=" + entidad.schema());
		}else {
			query.append("INSERT INTO ").append(clase.getSimpleName());
		}
	
		query.append("(");

		StringBuilder params = new StringBuilder();
		StringBuilder cols = new StringBuilder();

		Field[] attributes = clase.getDeclaredFields();
		for (Field field : attributes) {
			Columna col = (Columna) field.getAnnotation(Columna.class);

			if (col == null)
				break;

			System.out.println("col_name=" + col.name() + ",is_pk=" + col.isPk());
			cols.append(col.name()).append(",");
			params.append("?").append(",");
		}
		query.append(cols.substring(0, cols.length() - 1)).append(")");
		query.append(" VALUES (").append(params.substring(0, params.length() - 1)).append(")");

		return query.toString();
	}

}
