package ru.nsu.leontev.db.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.nsu.leontev.db.entity.TaskInfo;
import ru.nsu.leontev.model.Status;

import java.util.List;
import java.util.Optional;

public interface TaskInfoRepository extends MongoRepository<TaskInfo, String> {
    List<TaskInfo> findAllByStatus(Status status);
    Optional<TaskInfo> findFirstByStatus(Status status);
}
