package br.com.generic.dao.rules;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.com.generic.dao.Parameter;

public class InRule extends BaseRule {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Predicate getPredicate(Class<?> entityClass, CriteriaBuilder builder, Root<T> root,
			Parameter parameter) {
		String lasProperty = getLastProperty(parameter.getProperty());
		String navigation = getNavigation(parameter.getProperty());
		
		Path<T> path = this.<T>getPath(entityClass, root, navigation);
		if(isFrom(entityClass, path, parameter.getProperty())){
			return builder.in(((From)path).join(lasProperty)).in(parameter.getValue());
		}
		return builder.in(((From)path).get(lasProperty)).in(parameter.getValue());
	}

}
