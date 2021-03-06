package org.apereo.cas.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.wsfederation.web.flow.WsFederationAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link WsFederationWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for wsfed integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class WsFederationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String WS_FEDERATION_ACTION = "wsFederationAction";
    private static final String WS_FEDERATION_REDIRECT = "wsFederationRedirect";
    
    public WsFederationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                         final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                         final ApplicationContext applicationContext,
                                         final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            createEndState(flow, WS_FEDERATION_REDIRECT, "flowScope." + WsFederationAction.PROVIDERURL, true);
            final ActionState actionState = createActionState(flow, WS_FEDERATION_ACTION, createEvaluateAction(WS_FEDERATION_ACTION));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, WS_FEDERATION_REDIRECT));

            final String currentStartState = getStartState(flow).getId();
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_PROCEED, currentStartState));
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
            actionState.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));
            registerMultifactorProvidersStateTransitionsIntoWebflow(actionState);
            setStartState(flow, actionState);
        }
    }
}
