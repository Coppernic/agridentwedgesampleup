# agridentwedgesample

## Overview

This sample demonstrates how to interact with the Agrident module using COne2 while respecting its busy states. Proper communication protocols are crucial to ensure the module operates correctly and to prevent any potential damage.

## Important Guidelines

1. **Response Waiting**:
    - Always wait for a response from the module before sending a new command. This is especially important when configuring the module, as configuration changes require writing to the module's EEPROM memory.

2. **Avoid Powering Off During Communication**:
    - Do not power off the module while it is rewriting a new configuration. Only power off the module after it has finished the configuration write process to avoid corruption of the module's memory.

3. **Limit Configuration Changes**:
    - Refrain from changing settings excessively. Making hundreds or thousands of config changes in a short period can permanently damage the EEPROM.

## Conclusion

Following these guidelines will help maintain the Agrident module's longevity and ensure its proper functionality. For further assistance or inquiries, please reach out to Coppernic support.