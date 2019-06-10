package habr.demo.app.web;

import habr.demo.app.service.EnvironmentProvider;
import habr.demo.app.service.VersionProvider;
import habr.demo.app.web.model.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoEndpoint {

    @Autowired
    private VersionProvider version;
    @Autowired
    private EnvironmentProvider environment;

    @GetMapping("/info")
    public Info getVersion() {
        return new Info(
                "demo-app",
                version.getVersion(),
                environment.getEnvironment()
        );
    }
    
    @GetMapping("/get")
    public Info getHi() {
        return new Info(
                "Coffee!",
                version.getVersion(),
                environment.getEnvironment()

        );
    }
}
