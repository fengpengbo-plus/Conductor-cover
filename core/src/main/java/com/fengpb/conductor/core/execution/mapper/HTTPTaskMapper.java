 /*
  * Copyright 2020 Netflix, Inc.
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
 import com.fengpb.conductor.common.metadata.tasks.TaskDef;
 import com.fengpb.conductor.common.metadata.workflow.WorkflowTask;
 import com.fengpb.conductor.common.run.Workflow;
 import com.fengpb.conductor.core.dao.MetadataDAO;
 import com.fengpb.conductor.core.execution.ParametersUtils;
 import lombok.extern.slf4j.Slf4j;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;

 import javax.annotation.Resource;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Objects;
 import java.util.Optional;

 @Slf4j
 @Component(value = "HTTP")
 public class HTTPTaskMapper implements TaskMapper {

     @Autowired
     private final ParametersUtils parametersUtils;
     @Resource
     private final MetadataDAO metadataDAO;

     public HTTPTaskMapper(ParametersUtils parametersUtils, MetadataDAO metadataDAO) {
         this.parametersUtils = parametersUtils;
         this.metadataDAO = metadataDAO;
     }

     @Override
     public List<Task> getMappedTasks(TaskMapperContext taskMapperContext) {

         log.debug("TaskMapperContext {} in HTTPTaskMapper", taskMapperContext);

         WorkflowTask taskToSchedule = taskMapperContext.getTaskToSchedule();
         taskToSchedule.getInputParameters().put("asyncComplete", taskToSchedule.isAsyncComplete());
         Workflow workflowInstance = taskMapperContext.getWorkflowInstance();
         String taskId = taskMapperContext.getTaskId();
         int retryCount = taskMapperContext.getRetryCount();

         TaskDef taskDefinition = Optional.ofNullable(taskMapperContext.getTaskDefinition())
                 .orElseGet(() -> Optional.ofNullable(metadataDAO.getTaskDef(taskToSchedule.getName()))
                         .orElse(null));

         Map<String, Object> input = parametersUtils.getTaskInput(taskToSchedule.getInputParameters(), workflowInstance, taskId);
         Boolean asynComplete = (Boolean)input.get("asyncComplete");

         Task httpTask = new Task();
         httpTask.setTaskType(taskToSchedule.getType().name());
         httpTask.setTaskDefName(taskToSchedule.getName());
         httpTask.setReferenceTaskName(taskToSchedule.getTaskReferenceName());
         httpTask.setWorkflowInstanceId(workflowInstance.getWorkflowId());
//         httpTask.setWorkflowType(workflowInstance.getWorkflowName());
//         httpTask.setCorrelationId(workflowInstance.getCorrelationId());
         httpTask.setScheduledTime(System.currentTimeMillis());
         httpTask.setTaskId(taskId);
         httpTask.setInputData(input);
         httpTask.getInputData().put("asyncComplete", asynComplete);
         httpTask.setStatus(Task.Status.SCHEDULED);
//         httpTask.setRetryCount(retryCount);
//         httpTask.setCallbackAfterSeconds(taskToSchedule.getStartDelay());
         httpTask.setWorkflowTask(taskToSchedule);
//         httpTask.setWorkflowPriority(workflowInstance.getPriority());
//         if (Objects.nonNull(taskDefinition)) {
//             httpTask.setRateLimitPerFrequency(taskDefinition.getRateLimitPerFrequency());
//             httpTask.setRateLimitFrequencyInSeconds(taskDefinition.getRateLimitFrequencyInSeconds());
//             httpTask.setIsolationGroupId(taskDefinition.getIsolationGroupId());
//             httpTask.setExecutionNameSpace(taskDefinition.getExecutionNameSpace());
//         }
         return Collections.singletonList(httpTask);
     }
 }
