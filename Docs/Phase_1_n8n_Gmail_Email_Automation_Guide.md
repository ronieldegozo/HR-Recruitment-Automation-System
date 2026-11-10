# Phase 1: n8n Web + Gmail Email Automation Guide

> **Purpose:** Build and visibly test the first RecruitFlow automation in the n8n web editor. A new Gmail message triggers a workflow, the workflow checks for a CV attachment, and Gmail sends a receipt acknowledgment to a controlled test address.
>
> **Phase 1 scope:** Prove Gmail intake and acknowledgment. This guide does not connect a database, process CV contents, make hiring decisions, or deploy a production recruitment workflow.

## What you will build

```text
Gmail inbox
    ↓ new email
Gmail Trigger in n8n
    ↓ message ID
Gmail: Get message
    ↓ inspect sender, subject, message content, and attachment data
IF: allowed test sender AND expected attachment
    ↓ yes
Gmail: send acknowledgment to the test address
    ↓
Inspect execution in n8n and verify the message in Gmail Sent
```

For this first proof of concept, use a Gmail account and a separate mailbox you control as the sender. The workflow is configured to send only to the test recipient you set. Do not point it at a real applicant inbox or use real applicant CVs during testing.

## Before you start

- An n8n Cloud account, signed in to the **web editor**.
- A Gmail account that will receive test applications and send acknowledgments. A dedicated test Gmail account is recommended.
- A second mailbox you control to send test messages from.
- A small, non-sensitive sample PDF CV and DOCX CV. Use fictional details or a blank test document, not personal or applicant information.
- Access to Google Cloud Console if n8n asks you to configure your own Google OAuth app. In many n8n Cloud setups, you can connect Gmail using the built-in OAuth flow; follow the credential screen shown in your account.

> **UI note:** n8n periodically updates its editor. Button names and node fields may vary slightly by version. If a label differs, use the equivalent Gmail Trigger or Gmail action described here.

## 1. Prepare a test-only Gmail inbox

1. Sign in to the Gmail account you intend to connect to n8n.
2. Use a dedicated test inbox if possible. Avoid connecting a mailbox that contains private or production applicant data.
3. From your second mailbox, send a test email to the connected inbox with a subject such as `TEST - Application - Sample Position`.
4. Attach a non-sensitive PDF. Repeat later with a DOCX attachment.
5. Record the exact sender address. You will use it to ensure the test workflow only acknowledges your own test messages.

## 2. Connect Gmail to n8n

1. Open your n8n Cloud workspace in a browser and select **Workflows**.
2. Create a new workflow. Name it `WF-01 - Gmail Applicant Intake POC`.
3. Add a **Gmail Trigger** node to the canvas.
4. In the node's **Credential** field, choose **Create new credential** (or select an existing test credential).
5. Follow the Google sign-in window and choose the test Gmail account. Review the requested access and authorize it only if it matches the Gmail trigger and send actions you intend to test.
6. If the built-in connection is unavailable and n8n asks for a custom OAuth client, follow the **Custom Google OAuth setup** below.
7. Save the credential in n8n. Never paste a client secret, refresh token, or password into a workflow field or a committed file.
8. Return to the Gmail Trigger and select the credential you just connected.

### Custom Google OAuth setup (only if n8n requires it)

Google and n8n can change the exact screens, so treat the steps below as the general route and follow any callback URL and scope values displayed by your n8n credential page.

1. Open [Google Cloud Console](https://console.cloud.google.com/) and create or select a project for this test integration.
2. Enable the **Gmail API** for the project.
3. Open **Google Auth Platform** and configure the app information and audience. For an external testing app, add the Google account you will connect as a **test user**.
4. Create an OAuth client of the application type requested by n8n. Copy the **authorized redirect URI** shown in the n8n credential form exactly into Google Cloud; do not guess it.
5. Copy the OAuth client ID and client secret into the corresponding n8n credential fields, then save.
6. Return to n8n, connect the test Gmail account, and complete Google's consent flow.
7. Request only the Gmail permissions needed for the trigger and acknowledgment. Gmail access can be sensitive; keep the app in testing and use a dedicated mailbox during this proof of concept.

## 3. Configure the Gmail Trigger

1. In **Gmail Trigger**, select the event that watches for a new message (often named **Message Received**).
2. Set the polling interval to a short interval suitable for testing, such as every minute, if the editor offers interval settings.
3. If a search or filter field is available, use a test-only condition such as `from:your-test-sender@example.com`. Replace the example with the exact second mailbox address you control. You can also filter by a unique subject prefix such as `TEST -`.
4. Keep the message ID available. With Simplify enabled, the trigger returns IDs and headers such as From and Subject. Use a Gmail Get message action next to retrieve message content and attachment data.
5. Save the node.
6. Select **Fetch Test Event** / **Test step** / **Listen for event**, depending on the editor version.
7. While the node is listening, send a new test email from your second mailbox to the connected Gmail inbox. Include the test subject and a sample PDF attachment.
8. Return to n8n and inspect the trigger output. Confirm the sender, subject, and message ID are present. Attachment data is fetched in the next step.

If test data does not appear, send a fresh email after starting the test listener. Trigger test modes generally wait for a new event and do not necessarily replay old inbox messages.

## 4. Fetch the message and check the sender and attachment

The Gmail Trigger detects the message and gives you its ID and headers. Add a Gmail action to retrieve the message before testing attachment conditions.

1. Add a **Gmail** node after the Gmail Trigger.
2. Select the same credential, set **Resource** to **Message**, and **Operation** to **Get**.
3. For **Message ID**, use the expression picker to select the message ID from the Gmail Trigger output. Do not type a guessed property path.
4. Keep **Simplify** enabled for an easier-to-read result, or disable it if attachment fields are not visible in your n8n version. Execute the node with a test email and inspect its output for message text and attachment data. If the node exposes an option to download message attachments, enable it for this harmless test message.
5. Add an **IF** node after the Gmail Get action. Compare the message sender with the exact test sender address you control; use the expression picker to select the sender field from the fetched message.
6. Add a condition for the attachment. Check that at least one attachment exists. If the fetched data exposes filename and MIME type, accept only the intended test types (PDF and DOCX), checking the reported type as well as the filename where possible.
7. Combine conditions so the acknowledgment path runs only when the sender is the approved test sender and the expected attachment exists.
8. Route missing or unsupported attachments to a separate no-send path. During this POC, do not email a rejection. Inspect the execution and record that it was skipped or failed validation.
9. Execute the nodes with test data. Confirm a valid test message follows the yes path and a message without an attachment follows the no path.

> **File safety:** A filename or MIME type alone does not prove that a file is safe. This first phase only checks that a test attachment is present and of an expected declared type; it does not open or trust the file. Do not download, execute, or forward untrusted attachments.
## 5. Add the Gmail acknowledgment action

1. Add a **Gmail** node to the valid/yes output of the IF node.
2. Select the same Gmail credential.
3. Set **Resource** to **Message** and **Operation** to **Send** (or select the equivalent send-message action).
4. In **To**, use your own controlled test recipient while validating. Do not dynamically use an arbitrary incoming sender address until the test is complete and you have added the sender allowlist above.
5. Use a clear test subject, for example `TEST - Application received - Sample Position`.
6. Set email type to **Text** and use a simple body such as:

   ```text
   Hello,

   This is an automated test acknowledgment. The RecruitFlow test inbox received your sample application email and attachment.

   This is a test message. No real application has been submitted or reviewed.

   RecruitFlow Test
   ```

7. If the Gmail node adds an n8n attribution footer, review it in the test email. You may turn off that option if you prefer the message to contain only the text above.
8. Save the workflow.
9. Run a test execution with a new message from the allowlisted test sender. Confirm that the Gmail node runs only on the valid path.
10. Check the recipient mailbox and the connected account's **Sent** folder to verify the message was sent once.

## 6. Test the workflow in the n8n web editor

Use a fresh message for each case. Start the Gmail Trigger test listener or activate the workflow only after confirming the recipient is your controlled test mailbox.

| Test | Email setup | Expected result |
|---|---|---|
| PDF with allowed test sender | Send from your second mailbox; attach a harmless sample PDF | Trigger and Get outputs show sender, subject, message ID, and attachment; acknowledgment arrives at the controlled test address |
| DOCX with allowed test sender | Same, with a harmless sample DOCX | Trigger receives the message; acknowledgment follows the configured valid path |
| No attachment | Allowed test sender, no file attached | Trigger receives it; IF node routes to no-send path; no acknowledgment sent |
| Unsupported attachment | Allowed test sender, attach a harmless `.txt` or image file | No-send path; no acknowledgment sent |
| Non-allowlisted sender | Send from a different mailbox you control | Sender check fails; no acknowledgment sent |
| Replay / duplicate check | Re-run a prior execution if your editor supports it | Confirm the test behavior; understand that a manual execution replay may resend email. Do not replay a send step unless the recipient is your own test mailbox. |

### Inspecting an execution

1. Open **Executions** in the n8n editor (or select the latest execution from the workflow page).
2. Select the test execution.
3. Click each node to inspect its input, output, and status.
4. Confirm the Gmail Trigger received the expected message and that the IF node selected the expected branch.
5. Confirm the Gmail send node ran only for the valid test case.
6. If a node fails, record the error text without copying secrets or private email contents into project documentation.

## 7. Save and export the workflow

1. Save the workflow with the name `WF-01 - Gmail Applicant Intake POC`.
2. Use n8n's workflow menu to **Download** or **Export** the workflow as JSON.
3. In the RecruitFlow repository, store the export at `automation/n8n-workflows/WF-01-gmail-applicant-intake-poc.json` after the repository structure exists.
4. Before committing an export, inspect it for personal email addresses, message contents, tokens, credential values, or other private data. n8n credentials are normally stored separately, but check the export rather than assuming it is clean.
5. Document the test date, n8n workspace type, test cases run, and outcomes in the project's development notes. Do not include credentials or applicant data.

## 8. Leave the workflow in a safe state

- Keep it inactive until you are ready to run controlled tests.
- Before activating it, re-check the Gmail credential, trigger filters, sender allowlist, and fixed test recipient.
- Use a dedicated test inbox and never test against actual applicant email addresses.
- Keep secrets in n8n credentials or a secrets manager, not in node fields, screenshots, JSON exports, or source control.
- Limit who can access the n8n workspace and the connected Gmail account.
- Remember that retries and manual replays can send another acknowledgment. Verify the recipient before retrying a failed execution.
- Do not add AI assessment, CV extraction, database writes, candidate statuses, or automated hiring/rejection in Phase 1.

## Troubleshooting

| Symptom | Checks |
|---|---|
| Gmail credential will not connect | Confirm the selected Google account; if using custom OAuth, compare the redirect URI exactly with the value shown by n8n, confirm Gmail API is enabled, and check the OAuth app's test-user list. |
| Trigger test returns no data | Start the test listener first, then send a new message. Check the poll interval, sender/search filters, and that the message arrived in the connected inbox. |
| Message arrives but attachment is missing | Confirm the Gmail Get node is using the trigger message ID. Inspect the Get node output and its attachment/download options for your n8n version. Send a fresh test email with a small sample file. |
| IF condition goes to the wrong output | Open the trigger's output and use the expression picker to choose the actual sender and attachment fields. Compare exact values and account for whether sender data includes a display name. |
| Gmail send node fails | Reconnect the Gmail credential, verify the recipient address, inspect the node's error details, and avoid retrying until you confirm whether the first attempt already sent. |
| A duplicate acknowledgment arrives | Check for replayed executions, repeated trigger events, and multiple active copies of the workflow. Keep the recipient fixed to your test mailbox while investigating. |

## Phase 1 completion checklist

- [ ] n8n web editor workflow is saved as `WF-01 - Gmail Applicant Intake POC`.
- [ ] Gmail Trigger receives a new test message and exposes its sender, subject, and message ID; Gmail Get retrieves the message and attachment data.
- [ ] A PDF test message follows the valid path.
- [ ] A DOCX test message follows the valid path.
- [ ] Missing and unsupported attachment cases are routed to a no-send path.
- [ ] A non-allowlisted sender is not acknowledged.
- [ ] Acknowledgment arrives in the controlled test mailbox and is visible in Gmail Sent.
- [ ] Execution results are inspectable in n8n.
- [ ] Workflow JSON is exported and checked for sensitive information before committing.
- [ ] No database, AI assessment, or production applicant inbox is involved.

## Official references

- [n8n Gmail Trigger node](https://docs.n8n.io/integrations/builtin/trigger-nodes/n8n-nodes-base.gmailtrigger/)
- [n8n Gmail message operations](https://docs.n8n.io/integrations/builtin/app-nodes/n8n-nodes-base.gmail/message-operations/)
- [n8n Google OAuth2 credentials](https://docs.n8n.io/integrations/builtin/credentials/google/oauth-single-service/)
- [Google Gmail API setup](https://developers.google.com/workspace/gmail/api/quickstart/js)
- [Google Gmail API scopes](https://developers.google.com/workspace/gmail/api/auth/scopes)




