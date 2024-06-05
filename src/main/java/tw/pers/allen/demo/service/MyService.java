package tw.pers.allen.demo.service;

import org.springframework.stereotype.Service;

@Service
public class MyService {

	public String sayHi(String word) {
		return "Hello, %s !".formatted(word);
	}
}
