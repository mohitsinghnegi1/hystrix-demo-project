import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class PrintAnimalHystrixCommand  extends HystrixCommand<String> {

    private final String animalName;

    protected PrintAnimalHystrixCommand(HystrixCommandGroupKey group, String animalName) {
        super(group);
        this.animalName = animalName;
    }

    @Override
    protected String run() throws Exception {
        return String.format("Animal is %s",animalName );
    }
}
