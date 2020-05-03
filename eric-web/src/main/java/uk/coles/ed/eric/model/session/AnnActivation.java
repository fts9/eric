package uk.coles.ed.eric.model.session;

public class AnnActivation {
    private final float ACTIVATION_CEILING = 1.0f;	//Define the ceiling of the activation value
    private final float ACTIVATION_FLOOR = 0.0f; //Define the floor of the activation value
    private final float ACTIVATION_STEP = 0.1f;	//	Define default increment/decrement of ANN activation

    private float activation = ACTIVATION_FLOOR;
    private int activationCount;

    public float getActivation() {
        return activation;
    }

    public int getActivationCount() {
        return activationCount;
    }

    public void incrementActivationCount() {
        activationCount++;
    }

    public void preActivate(float weight) {
        setActivation(activation + weight);
    }

    public void enhance() {
        setActivation(activation + ACTIVATION_STEP);
    }

    public void inhibit() {
        setActivation(activation - ACTIVATION_STEP);
    }

    private void setActivation(float activation) {
        if(activation > ACTIVATION_CEILING) {
            activation = ACTIVATION_CEILING;
        }
        if(activation < ACTIVATION_FLOOR) {
            activation = ACTIVATION_FLOOR;
        }
        this.activation = activation;
    }
}
