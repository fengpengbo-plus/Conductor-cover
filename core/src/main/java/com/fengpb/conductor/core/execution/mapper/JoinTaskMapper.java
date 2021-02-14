/*
 * Copyright 2018 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fengpb.conductor.core.execution.mapper;

import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.common.metadata.workflow.WorkflowTask;
import com.fengpb.conductor.common.run.Workflow;
import com.fengpb.conductor.core.execution.SystemTaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("JOIN")
public class JoinTaskMapper implements TaskMapper {

    @Override
    public List<Task> getMappedTasks(TaskMapperContext taskMapperContext) {

        log.debug("TaskMapperContext {} in JoinTaskMapper", taskMapperContext);

        WorkflowTask taskToSchedule = taskMapperContext.getTaskToSchedule();
        Workflow workflowInstance = taskMapperContext.getWorkflowInstance();
        String taskId = taskMapperContext.getTaskId();

        Map<String, Object> joinInput = new HashMap<>();
        joinInput.put("joinOn", taskToSchedule.getJoinOn());

        Task joinTask = new Task();
        joinTask.setTaskType(SystemTaskType.JOIN.name());
        joinTask.setTaskDefName(SystemTaskType.JOIN.name());
        joinTask.setReferenceTaskName(taskToSchedule.getTaskReferenceName());
        joinTask.setWorkflowInstanceId(workflowInstance.getWorkflowId());
        joinTask.setScheduledTime(System.currentTimeMillis());
        joinTask.setStartTime(System.currentTimeMillis());
        joinTask.setInputData(joinInput);
        joinTask.setTaskId(taskId);
        joinTask.setStatus(Task.Status.IN_PROGRESS);
        joinTask.setWorkflowTask(taskToSchedule);

        return Collections.singletonList(joinTask);
    }
}
