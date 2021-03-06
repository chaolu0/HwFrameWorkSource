package org.bouncycastle.jcajce.provider.config;

import java.util.Map;
import java.util.Set;
import javax.crypto.spec.DHParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;

public interface ProviderConfiguration {
    Set getAcceptableNamedCurves();

    Map getAdditionalECParameters();

    DHParameterSpec getDHDefaultParameters(int i);

    ECParameterSpec getEcImplicitlyCa();
}
