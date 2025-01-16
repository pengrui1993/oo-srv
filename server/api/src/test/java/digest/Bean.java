package digest;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Bean {
    private String name;
    public static void main(String[] args) {
        var value = new Bean().setName("").getName();
    }
}
