package com.smartsparrow.mercury.simulator.scenario.math;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioRunner;

@Scenario("MathAssetOk")
@RequestMapping(value = "/services/rest/demo/plugins/app/showimage", method = RequestMethod.POST)
public class MathAssetOkScenario extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioRunner runner) {
        runner.http()
                .receive(builder -> builder.post());

        runner.http()
                .send((builder -> builder
                        .response(HttpStatus.OK)
                        .payload("{\n" +
                                         "    \"status\": \"ok\",\n" +
                                         "    \"result\": {\n" +
                                         "        \"height\": \"20\",\n" +
                                         "        \"width\": \"34\",\n" +
                                         "        \"content\": \"<svg xmlns=\\\"http://www.w3.org/2000/svg\\\" xmlns:wrs=\\\"http://www.wiris.com/xml/mathml-extension\\\" height=\\\"20\\\" width=\\\"34\\\" wrs:baseline=\\\"16\\\"><!--MathML: <math xmlns=\\\"http://www.w3.org/1998/Math/MathML\\\"><mn>1</mn><mo>-</mo><mn>2</mn></math>--><defs><style type=\\\"text/css\\\">@font-face{font-family:'math1da40657c9fece7e48d30af42d3';src:url(data:font/truetype;charset=utf-8;base64,AAEAAAAMAIAAAwBAT1MvMi7iBBMAAADMAAAATmNtYXDEvmKUAAABHAAAADRjdnQgDVUNBwAAAVAAAAA6Z2x5ZoPi2VsAAAGMAAAAcmhlYWQQC2qxAAACAAAAADZoaGVhCGsXSAAAAjgAAAAkaG10eE2rRkcAAAJcAAAACGxvY2EAHTwYAAACZAAAAAxtYXhwBT0FPgAAAnAAAAAgbmFtZaBxlY4AAAKQAAABn3Bvc3QB9wD6AAAEMAAAACBwcmVwa1uragAABFAAAAAUAAADSwGQAAUAAAQABAAAAAAABAAEAAAAAAAAAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAgICAAAAAg1UADev96AAAD6ACWAAAAAAACAAEAAQAAABQAAwABAAAAFAAEACAAAAAEAAQAAQAAIhL//wAAIhL//93vAAEAAAAAAAABVAMsAIABAABWACoCWAIeAQ4BLAIsAFoBgAKAAKAA1ACAAAAAAAAAACsAVQCAAKsA1QEAASsABwAAAAIAVQAAAwADqwADAAcAADMRIRElIREhVQKr/asCAP4AA6v8VVUDAAABAIABVQLVAasAAwAwGAGwBBCxAAP2sAM8sQIH9bABPLEFA+YAsQAAExCxAAblsQABExCwATyxAwX1sAI8EyEVIYACVf2rAatWAAAAAQAAAAEAANV4zkFfDzz1AAMEAP/////WOhNz/////9Y6E3MAAP8gBIADqwAAAAoAAgABAAAAAAABAAAD6P9qAAAXcAAA/7YEgAABAAAAAAAAAAAAAAAAAAAAAgNSAFUDVgCAAAAAAAAAACgAAAByAAEAAAACAF4ABQAAAAAAAgCABAAAAAAABAAA3gAAAAAAAAAVAQIAAAAAAAAAAQASAAAAAAAAAAAAAgAOABIAAAAAAAAAAwAwACAAAAAAAAAABAASAFAAAAAAAAAABQAWAGIAAAAAAAAABgAJAHgAAAAAAAAACAAcAIEAAQAAAAAAAQASAAAAAQAAAAAAAgAOABIAAQAAAAAAAwAwACAAAQAAAAAABAASAFAAAQAAAAAABQAWAGIAAQAAAAAABgAJAHgAAQAAAAAACAAcAIEAAwABBAkAAQASAAAAAwABBAkAAgAOABIAAwABBAkAAwAwACAAAwABBAkABAASAFAAAwABBAkABQAWAGIAAwABBAkABgAJAHgAAwABBAkACAAcAIEATQBhAHQAaAAgAEYAbwBuAHQAUgBlAGcAdQBsAGEAcgBNAGEAdABoAHMAIABGAG8AcgAgAE0AbwByAGUAIABNAGEAdABoACAARgBvAG4AdABNAGEAdABoACAARgBvAG4AdABWAGUAcgBzAGkAbwBuACAAMQAuADBNYXRoX0ZvbnQATQBhAHQAaABzACAARgBvAHIAIABNAG8AcgBlAAADAAAAAAAAAfQA+gAAAAAAAAAAAAAAAAAAAAAAAAAAuQcRAACNhRgAsgAAABUUE7EAAT8=)format('truetype');font-weight:normal;font-style:normal;}</style></defs><text font-family=\\\"Arial\\\" font-size=\\\"16\\\" text-anchor=\\\"middle\\\" x=\\\"4.5\\\" y=\\\"16\\\">1</text><text font-family=\\\"math1da40657c9fece7e48d30af42d3\\\" font-size=\\\"16\\\" text-anchor=\\\"middle\\\" x=\\\"16.5\\\" y=\\\"16\\\">&#x2212;</text><text font-family=\\\"Arial\\\" font-size=\\\"16\\\" text-anchor=\\\"middle\\\" x=\\\"28.5\\\" y=\\\"16\\\">2</text></svg>\",\n" +
                                         "        \"baseline\": \"16\",\n" +
                                         "        \"format\": \"svg\",\n" +
                                         "        \"role\": \"math\"\n" +
                                         "    }\n" +
                                         "}"))
                );
    }
}
