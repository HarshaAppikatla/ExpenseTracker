/**
 * Package-level Hibernate filter definitions for the entity package.
 *
 * Defining @FilterDef at the package level (rather than on @MappedSuperclass) prevents
 * Hibernate from registering the filter condition twice — once at the superclass mapping
 * level and once at the concrete entity level — which was causing duplicate "is_deleted = 0"
 * clauses in generated SQL.
 *
 * The filter is still APPLIED to entities via @Filter(name = "deletedFilter") on BaseEntity
 * and is ENABLED at runtime by HibernateFilterAspect.
 */
@FilterDef(name = "deletedFilter", defaultCondition = "is_deleted = 0")
package com.expenseflow.entity;

import org.hibernate.annotations.FilterDef;
