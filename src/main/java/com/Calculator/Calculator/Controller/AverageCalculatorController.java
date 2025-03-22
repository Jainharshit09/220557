package com.Calculator.Calculator.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/number")
public class AverageCalculatorController {
    private static final Logger logger = Logger.getLogger(AverageCalculatorController.class.getName());
    private static final int TIMEOUT_MS = 500;

    @Value("${PRIME_API_URL}") private String primeApiUrl;
    @Value("${FIBO_API_URL}") private String fiboApiUrl;
    @Value("${EVEN_API_URL}") private String evenApiUrl;
    @Value("${RANDOM_API_URL}") private String randomApiUrl;

    private static final int WINDOW_SIZE = 10;
    private final Queue<Integer> numberWindow = new ConcurrentLinkedQueue<>();
    private final Set<Integer> uniqueNumbers = ConcurrentHashMap.newKeySet();
    private final RestTemplate restTemplate;

    public AverageCalculatorController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
    }

    @GetMapping("/{type}")
    public ResponseEntity<Map<String, Object>> getNumbers(@PathVariable String type) {
        String apiUrl = getApiUrl(type);
        if (apiUrl == null) return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid type"));

        List<Integer> newNumbers = fetchNumbers(apiUrl);
        if (newNumbers.isEmpty())
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(Collections.singletonMap("error", "Failed to fetch numbers"));

        List<Integer> prevState = new ArrayList<>(numberWindow);
        updateWindow(newNumbers);
        List<Integer> currState = new ArrayList<>(numberWindow);
        double avg = numberWindow.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        Map<String, Object> response = new HashMap<>();
        response.put("windowPrevState", prevState);
        response.put("windowCurrState", currState);
        response.put("numbers", newNumbers);
        response.put("avg", avg);
        return ResponseEntity.ok(response);
    }

    private String getApiUrl(String type) {
        return switch (type) {
            case "p" -> primeApiUrl;
            case "f" -> fiboApiUrl;
            case "e" -> evenApiUrl;
            case "r" -> randomApiUrl;
            default -> null;
        };
    }

    private List<Integer> fetchNumbers(String apiUrl) {
        CompletableFuture<List<Integer>> future = CompletableFuture.supplyAsync(() -> {
            try {
                ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, Map.class);
                if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null)
                    return Collections.emptyList();
                return (List<Integer>) response.getBody().get("numbers");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "API Request Failed: {0}", e);
                return Collections.emptyList();
            }
        });

        try {
            return future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "API Error: {0}", e);
            return Collections.emptyList();
        }
    }

    private void updateWindow(List<Integer> newNumbers) {
        for (int num : newNumbers) {
            if (uniqueNumbers.add(num)) {
                if (numberWindow.size() >= WINDOW_SIZE) uniqueNumbers.remove(numberWindow.poll());
                numberWindow.offer(num);
            }
        }
    }
}
