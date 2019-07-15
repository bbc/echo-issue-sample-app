# Echo Issue Sample App
See https://jira.dev.bbc.co.uk/browse/MOBIP-15199

This is a simple AV Statistics implementation which uses SMP and the Echo client
KEEPALIVE_DURATION is set to 1 second in order to make the issue faster to reproduce.

The app functions as follows:
1) Plays an episode in SMP
2) After 10 seconds (~10 keepalive events), it switches over to another episode
3) It can then be observed that there is still a refresh event sent with the first episode's countername, and  several page views for the first episode are also sent after the second episode has already begun

**First Episode ID:** b05w8l03

**Second Episode ID:** b05w8lhh

**Countername Format:** countername_versionId

For the first episode we have:
countername_b05w8l03.page

For the second episode:
countername_b05w8lhh.page

After 10 seconds has passed and the episode switches, this is when to keep an eye on which stats are firing and what the counternames are .

# Example Charles Session
There is an example Charles session in the repo which shows the behaviour -  after the 10 seconds of keepalive events the transition occurs.

https://github.com/bbc/echo-issue-sample-app/blob/f25c4324025cda20eefa6585dc7fbf7b6cb40316/charles-session/echo-issue-charles-session.chls
