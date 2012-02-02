package com.spaceprogram.simplejpa;

import java.util.Map;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import com.spaceprogram.simplejpa.util.PersistenceUnitInformation;

/**
 * Needs work.
 *
 * User: treeder
 * Date: Feb 10, 2008
 * Time: 6:25:13 PM
 */
public class PersistenceProviderImpl
        implements
            PersistenceProvider
{

    public PersistenceProviderImpl()
    {
    }

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(
            PersistenceUnitInfo persistenceUnitInfo,
            Map map)
    {
        System.out.println("createContainerEntityManagerFactory");
        return new EntityManagerFactoryImpl(persistenceUnitInfo, map);
    }

    @Override
    public EntityManagerFactory createEntityManagerFactory(
            String s,
            Map map)
    {
        System.out.println("createEntityManagerFactory");
        EntityManagerFactoryImpl emFactory = new EntityManagerFactoryImpl(s, map);
        PersistenceUnitInformation puInfo = PersistenceUnitInformation.foundPersistenceUnitInfoFor(s);
        loadExplicitClasses(puInfo.getExplicitClasses(), emFactory);
        loadExplicitJarFiles(puInfo.getExplicitJarFiles(), emFactory);
        return emFactory;
    }

    private void loadExplicitClasses(
            Set<String> explicitClasses,
            EntityManagerFactoryImpl emFactory)
    {
        for (String explicitClass : explicitClasses) {
            Class explicitClassType = loadExplicitClass(explicitClass);
            if (explicitClassType != null 
                    && explicitClassType.isAnnotationPresent(Entity.class)
                    && !emFactory.getEntityMap().containsKey(explicitClass)) {
                emFactory.initEntity(explicitClass);
            }
        }
    }
    
    private Class loadExplicitClass(String className) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return classLoader.loadClass(className);
        } catch (Exception exc) {
            return null;
        }
    }

    private void loadExplicitJarFiles(
            Set<String> explicitJarFiles,
            EntityManagerFactoryImpl emFactory)
    {
        if (!explicitJarFiles.isEmpty()) {
            emFactory.scanClasses(explicitJarFiles);
        }
    }
}
