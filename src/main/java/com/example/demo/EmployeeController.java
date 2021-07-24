package com.example.demo;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {
	
	@GetMapping(value = "/employee")
	public ResponseEntity<String> getEmployee() {
        return ResponseEntity.ok("Accessed API");
	}
}
