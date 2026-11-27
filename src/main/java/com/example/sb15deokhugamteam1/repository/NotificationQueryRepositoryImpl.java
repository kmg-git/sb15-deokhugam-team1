package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.dto.response.NotificationDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class NotificationQueryRepositoryImpl implements NotificationQueryRepository{

    public static final String ASC_OPERATOR = ">";
    public static final String DESC_OPERATOR = "<";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<NotificationDto> findPage(UUID userId, Sort.Direction direction, UUID cursorId, Instant after,
                                          int limit) {
        String operator = direction.isAscending() ? ASC_OPERATOR : DESC_OPERATOR;
        String order = direction.isAscending() ? "asc" : "desc";
        boolean hasCursor = after != null && cursorId != null;

        StringBuilder jpql = new StringBuilder()
                .append("select new ").append(NotificationDto.class.getName())
                .append("(n.id, n.user.id, n.review.id, r.content, n.message, n.confirmed, ")
                .append("n.createdAt, n.updatedAt) ")
                .append("from Notification n join n.review r ")
                .append("where n.user.id = :userId");

        if (hasCursor) {
            jpql.append(" and (n.createdAt ").append(operator).append(" :after").append(" or (n.createdAt = :after and n.id ")
                    .append(operator).append(":cursorId))");}
            jpql.append(" order by n.createdAt ").append(order).append(", n.id ").append(order);

            TypedQuery<NotificationDto> query = entityManager.createQuery(jpql.toString(), NotificationDto.class)
                    .setParameter("userId", userId).setMaxResults(limit + 1);
            if(hasCursor) {
                query.setParameter("after", after);
                query.setParameter("cursorId", cursorId);
            }
            return query.getResultList();
    }
}
