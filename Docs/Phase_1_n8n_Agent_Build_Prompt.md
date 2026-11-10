# RecruitFlow Phase 1 — Prompt for the n8n AI Agent

Use this prompt with the **n8n AI Workflow Builder / AI Assistant**. It gives the agent the project goal and asks it to build the workflow directly in the current n8n project. The expected manual step is connecting the Gmail OAuth credential; the agent should configure the rest and clearly identify any field it cannot complete.

## Prompt to paste into n8n

You are building the first working automation for **RecruitFlow**, a single-company HR Recruitment Management System. Build the workflow directly in my current n8n project and wire the nodes together. Do not only give me instructions or a diagram.

### Project goal and context

RecruitFlow will receive applicant CVs through a Gmail inbox, automatically acknowledge valid applications, extract and store application information in later phases, and help HR review candidates and manage recruitment. AI may assist HR in later phases, but it must never make final hiring or rejection decisions.

We are currently doing **Phase 1: a small n8n web-editor + Gmail proof of concept**. The purpose is to prove the email intake and acknowledgment flow before building the backend or database. This first workflow should watch a Gmail test/recruitment inbox, detect an incoming application email, check that it has a supported CV attachment, and send an acknowledgment to the applicant. We need to inspect each run in the n8n UI.

### Build this workflow
zzz
Name it **WF-01 - Gmail Applicant Intake POC**.

Create and connect the appropriate standard n8n nodes to implement this flow:

1. **Gmail Trigger:** start on a newly received email in the Gmail inbox I connect. Configure a suitable polling interval and expose the message ID, sender, subject, and received message data. Use a Gmail search filter only if it is useful and does not prevent normal application emails from being detected.
2. **Get the full message:** use the Gmail message ID from the trigger with a Gmail **Message: Get** action if needed to retrieve the message body and attachment details. Do not assume the trigger output includes the full attachment data.
3. **Validate the application email:** check that the sender address is present and that at least one attachment is present with an allowed type: PDF or DOCX. Check attachment size if the available node data supports it. A file extension or declared MIME type alone does not prove a file is safe; this POC only validates declared type and presence and must not execute or open an attachment.
4. **Valid path:** send a plain-text acknowledgment to the applicant’s sender address, using the sender field from the received message. Use this message:

   **Subject:** Application Received - [Job Position, if reliably available]

   **Body:**

   ```text
   Hello,

   Thank you for submitting your application.

   We have received your CV and application details. Our recruitment team will review your qualifications and experience.

   We aim to contact you within 24–48 hours regarding the next steps in the recruitment process.

   Thank you for your interest!

   Best regards,
   HR Recruitment Team
   RecruitFlow
   ```

   If the applicant name or job position is not reliably available from the email, use a generic greeting and a generic subject. Do not invent either value.
5. **Invalid/missing attachment path:** do not send an acknowledgment or a rejection. End this path clearly so I can see in the execution that the email was skipped because it did not meet the attachment check.
6. **Duplicate protection:** prevent the same Gmail message from causing repeated acknowledgment emails when an execution is retried or replayed. Prefer a built-in n8n Data Table keyed by Gmail message ID if available. If this is not practical with standard nodes, tell me exactly what remains to be configured and make the workflow’s duplicate risk visible; do not claim it is idempotent unless the workflow implements it.
7. Keep the workflow easy to inspect in the visual editor. Use clear node names and add brief notes to explain the valid and skipped paths.

### Credential and setup requirements

- Build all workflow nodes and connections that can be created before credentials are connected.
- Use the standard n8n credential selector for Gmail. Do not ask me to paste OAuth secrets, tokens, or passwords into the prompt, a node field, or workflow notes.
- Leave Gmail credential fields ready for me to select/create the credential. I will connect the Gmail test inbox and complete Google's OAuth consent flow myself.
- Do not require a separate AI model credential: this workflow does not need an AI node.
- Do not add external services or custom code unless a standard n8n node cannot meet a stated requirement. If a requirement truly needs another credential or manual configuration, explain why and list only the exact remaining setup steps.

### Scope and safety

- This is a proof of concept. Do not connect Spring Boot, MySQL, a database outside n8n, file storage, CV text extraction, AI assessment, hiring decisions, or production HR systems.
- Do not activate the workflow or send any email while building. Leave it inactive until I connect Gmail, review the nodes, and run a controlled test.
- Do not use sample applicant records or fabricate candidate details.
- Do not silently omit a requirement. If a Gmail node version does not expose a needed field (such as attachment metadata), build what is possible and tell me exactly which node option or manual field selection is missing.

### When you finish

1. Tell me the workflow name and summarize the nodes you created and how they are connected.
2. Tell me exactly where to select or create the Gmail credential and complete OAuth.
3. List any remaining manual configuration other than connecting Gmail, and explain why it cannot be completed by the builder.
4. Give me a short safe test sequence: valid PDF, valid DOCX, no attachment, unsupported file type, and retry/replay of the same message. Explain how to inspect each execution.
5. Confirm the workflow is still inactive and has not sent an email.

## What I need to do after the agent builds it

The n8n AI agent can build and wire the workflow, but it cannot authorize access to your Gmail account for you. You will need to:

1. Open the generated Gmail Trigger and Gmail actions, select or create the Gmail credential, and sign in through Google's OAuth consent screen.
2. Review the generated fields and any manual setup the agent identified.
3. Test using harmless sample messages and CV files in an inbox you control. Inspect the runs and confirm acknowledgment goes to the expected address.
4. Activate the workflow only when the tests pass and you are ready for the connected inbox to be monitored.

For the first tests, use a dedicated test inbox and fictional/sample CVs. When you are ready to use the actual recruitment inbox, review the recipient behavior and message wording before activating it there.

## Official references

- [n8n AI Workflow Builder best practices](https://blog.n8n.io/ai-workflow-builder-best-practices/)
- [n8n Gmail Trigger documentation](https://docs.n8n.io/integrations/builtin/trigger-nodes/n8n-nodes-base.gmailtrigger/)
- [n8n Gmail message operations](https://docs.n8n.io/integrations/builtin/app-nodes/n8n-nodes-base.gmail/message-operations/)
- [n8n Google OAuth2 credentials](https://docs.n8n.io/integrations/builtin/credentials/google/oauth-single-service/)
