const http = require('http');

// Helper to make HTTP requests
function request(method, path, body) {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: 'localhost',
            port: 8086,
            path: '/api/v1/auth' + path,
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };

        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    try {
                        resolve(data ? JSON.parse(data) : null);
                    } catch (e) {
                         // For 204 No Content
                        resolve(null);
                    }
                } else {
                    reject(new Error(`Request failed: ${res.statusCode} ${data}`));
                }
            });
        });

        req.on('error', reject);
        if (body) req.write(JSON.stringify(body));
        req.end();
    });
}

function decodeJwt(token) {
    if(!token) return null;
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
}

// Polyfill for atob in Node.js (older versions might need this, or Buffer)
function atob(str) {
    return Buffer.from(str, 'base64').toString('binary');
}


async function verify() {
    try {
        const authRequestId = "123e4567-e89b-12d3-a456-426614174000"; // Dummy UUID
        console.log(`[1] Init Auth with authRequestId: ${authRequestId}`);
        
        // 1. Init
        const initRes = await request('POST', '/init', {
            clientData: "01012345678",
            name: "JohnDoe",
            carrier: "SKT",
            authRequestId: authRequestId
        });
        console.log("[1] Response:", initRes);
        const { tokenId, otp } = initRes;

        // 2. Confirm
        console.log(`[2] Confirm Auth with OTP: ${otp}`);
        await request('PATCH', '/confirm', {
            tokenId: tokenId,
            otp: otp
        });
        console.log("[2] Success: 204 No Content");

        // 3. Status & JWT
        console.log(`[3] Get Status & JWT`);
        const statusRes = await request('GET', `/status/${tokenId}`);
        console.log("[3] Response:", statusRes);
        
        const jwt = statusRes.accessToken;
        if (!jwt) throw new Error("No AccessToken in response!");

        // 4. Decode Verify
        const decoded = decodeJwt(jwt);
        console.log("[4] Decoded JWT Payload:", decoded);

        if (decoded.ci) {
            console.log("✅ CI Verified:", decoded.ci);
        } else {
            console.error("❌ CI Missed!");
        }

        if (decoded.auth_request_id === authRequestId) {
            console.log("✅ auth_request_id Verified");
        } else {
            console.error("❌ auth_request_id Mismatch!", decoded.auth_request_id);
        }

    } catch (e) {
        console.error("Testing Failed:", e);
    }
}

verify();
