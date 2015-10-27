package br.com.generic.dao.rules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import br.com.generic.exceptions.BaseRuntimeException;

public abstract class BaseRule implements Rule{
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> Path<T> getPath(Class<?> entityClass, Root<T> root, String properties){
		
		if(properties.contains(".")){
			String[] ropertys =  properties.split("\\.");
			
			Path<T> path = root;
			Field field;
			for(int i = 0 ; i < (ropertys.length - 1) ; i++){
				field = getField(entityClass, properties, ropertys[i]);
				if(this.<T>responderJoin(path)){
					if(annotedEntity(field.getType()) || isCollectionEntity(field)){
						if(path instanceof Join)
							path = ((Join)path).join(ropertys[i]);
						else
							path = ((Root)path).join(ropertys[i]);
					}else{
						path = path.get(ropertys[i]);
					}
				}else{
					path = path.get(ropertys[i]);
				}
				
			}
			return path;
		}else{
			return root;
		}
	}
	
	protected Field getField(Class<?> entityClass, String properties, String node){
		String[] ropertys =  properties.split("\\.");
		Field field;
		Class<?> clazz = entityClass;
		for(int i = 0 ; i < ropertys.length ; i++){
			do{
				field = getField(clazz, ropertys[i]);
				if(field == null)
					clazz = clazz.getSuperclass();
			}while (field == null && !clazz.equals(Object.class));
			
			if(field != null && field.getName().equals(node)){
				return field;
			}else if(field != null){
				if(isCollectionEntity(field)){
			        clazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
				}else{
					clazz = field.getType();
				}
			}
		}
		return null;
	}
	
	protected <T> boolean responderJoin(Path<T> path){
		return path instanceof Join || path instanceof Root;
	}
	
	protected boolean annotedEntity(Class<?> clazz){
		return clazz.isAnnotationPresent(Entity.class);
	}
	protected Field getField(Class<?> entityClass, String fieldName){
		Field fieldReturn = null;;
		try {
			fieldReturn = entityClass.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
		} catch (SecurityException e) {
		}
		if(fieldReturn != null){
			return fieldReturn;
		}else if(!entityClass.getSuperclass().equals(Object.class)){
			return getField(entityClass.getSuperclass(), fieldName);
		}else{
			throw new BaseRuntimeException(fieldName + " not found in " + entityClass);
		}
	}
	protected boolean isCollectionEntity(Field field){
		return field.isAnnotationPresent(OneToMany.class) || 
				field.isAnnotationPresent(ManyToMany.class);
	}
	
	protected boolean isCollectionGetEntity(Class<?> entityClass, Field field){
		Method method = getGeterMethod(entityClass, field);
		if(method != null && 
				(field.isAnnotationPresent(OneToMany.class) || 
				field.isAnnotationPresent(ManyToMany.class))){
			return true;
		}else{
			return false;
		}
	}
	
	
	
	protected Method getGeterMethod(Class<?> entityClass, Field field){
		Method methodReturn = null;;
		
		for(Method method : entityClass.getMethods()){
			if(method.getName().replace("get", "").toLowerCase().equals(field.getName().toLowerCase())){
				 methodReturn = method;
			}
		}
		
		if(methodReturn != null){
			return methodReturn;
		}else if(!entityClass.getSuperclass().equals(Object.class)){
			return getGeterMethod(entityClass.getSuperclass(), field);
		}else{
			return null;
		}
	}
	
	protected String getLastProperty(String property){
		if(property.contains(".")){
			return property.substring(property.lastIndexOf(".") + 1);
		}else{
			return property;
		}
	}


}
