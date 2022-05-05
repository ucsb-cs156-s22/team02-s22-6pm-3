package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Api(description = "HelpRequest")
@RequestMapping("/api/helprequest")
@RestController
@Slf4j

public class HelpRequestController extends ApiController {
    
    @Autowired
    HelpRequestRepository helpRequestRepository;

    @ApiOperation(value = "List all help requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<HelpRequest> allHelpRequests(){
        Iterable<HelpRequest> requests = helpRequestRepository.findAll();
        return requests;
    }   

    @ApiOperation(value = "Get a single request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public HelpRequest getById(
            @ApiParam("Request ID") @RequestParam Long id){
        HelpRequest helpRequest = helpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

        return helpRequest;
    }

    @ApiOperation(value = "Create a new request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public HelpRequest postHelpRequest(
        @ApiParam("Requester Email") @RequestParam String requesterEmail,
        @ApiParam("Team's Id") @RequestParam String teamId,
        @ApiParam("Location of requester (table/breakoutroom)") @RequestParam String tableOrBreakoutRoom,
        @ApiParam("Date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam LocalDateTime requestTime,
        @ApiParam("Explanation of problem that needs to be solved") @RequestParam String explanation,
        @ApiParam("Has the issue been solved (true/false)") @RequestParam boolean solved)
        {
        HelpRequest request = new HelpRequest(); 
        request.setRequesterEmail(requesterEmail);
        request.setTeamId(teamId);
        request.setTableOrBreakoutRoom(tableOrBreakoutRoom);
        request.setRequestTime(requestTime);
        request.setExplanation(explanation);
        request.setSolved(solved);

        HelpRequest savedHelpRequest = helpRequestRepository.save(request); 

        return savedHelpRequest; 
    }

    @ApiOperation(value = "Delete a HelpRequest")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteHelpRequest(
            @ApiParam("Request ID") @RequestParam Long id){
        HelpRequest helpRequest = helpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));
    
        helpRequestRepository.delete(helpRequest);
        return genericMessage("HelpRequest with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public HelpRequest updateHelpRequest(
        @ApiParam("Request ID") @RequestParam Long id,
        @RequestBody @Valid HelpRequest incoming
    ){

    HelpRequest request = helpRequestRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));
    
    request.setRequesterEmail(incoming.getRequesterEmail());
    request.setTeamId(incoming.getTeamId());
    request.setTableOrBreakoutRoom(incoming.getTableOrBreakoutRoom());
    request.setRequestTime(incoming.getRequestTime());
    request.setExplanation(incoming.getExplanation());
    request.setSolved(incoming.getSolved());

    helpRequestRepository.save(request);
    
    return request;
    }
}
