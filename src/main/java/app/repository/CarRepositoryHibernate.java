package app.repository;

import app.domain.Car;

import java.math.BigDecimal;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.hibernate.cfg.Configuration;

public class CarRepositoryHibernate implements CarRepository{

    private EntityManager entityManager;

    public CarRepositoryHibernate() {
        entityManager = new Configuration()
                .configure("hibernate/postgres.cfg.xml")
                .buildSessionFactory()
                .createEntityManager();
    }

    @Override
    public List<Car> getAll() {
        TypedQuery<Car> query = entityManager.createQuery("SELECT c FROM Car c", Car.class);
        return query.getResultList();
    }

    @Override
    public List<Car> getCarsWithParameters(BigDecimal minPrice, BigDecimal maxPrice, String brand, String orderBy, String order, int limit) {
        StringBuilder queryBuilder = new StringBuilder("SELECT c FROM Car c WHERE 1=1");
        if (minPrice != null) {
            queryBuilder.append(" AND c.price >= :minPrice");
        }
        if (maxPrice != null) {
            queryBuilder.append(" AND c.price <= :maxPrice");
        }
        if (brand != null && !brand.isEmpty()) {
            queryBuilder.append(" AND c.brand = :brand");
        }
        queryBuilder.append(" ORDER BY c.").append(orderBy).append(" ").append(order);

        TypedQuery<Car> query = entityManager.createQuery(queryBuilder.toString(), Car.class);
        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        if (brand != null && !brand.isEmpty()) {
            query.setParameter("brand", brand);
        }
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public Car save(Car car) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(car);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e; // Transaction cancelled
        }
        return car;
    }

    @Override
    public Car getById(long id) {
        return entityManager.find(Car.class, id);
    }

    @Override
    public void updatePrice(long id, BigDecimal newPrice) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Car car = entityManager.find(Car.class, id);
            if (car != null) {
                car.setPrice(newPrice);
                entityManager.merge(car);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public void deleteById(long id) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Car car = entityManager.find(Car.class, id);
            if (car != null) {
                entityManager.remove(car);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
